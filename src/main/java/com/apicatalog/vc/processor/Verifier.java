package com.apicatalog.vc.processor;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.SignatureSuiteProvider;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.method.DidUrlMethodResolver;
import com.apicatalog.ld.signature.method.HttpMethodResolver;
import com.apicatalog.ld.signature.method.MethodResolver;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.ld.signature.proof.EmbeddedProof;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.model.Credential;
import com.apicatalog.vc.model.Verifiable;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class Verifier extends Processor<Verifier> {

    final SignatureSuiteProvider suiteProvider;

    private final URI location;
    private final JsonObject document;

    private final Map<String, Object> params;

    private Collection<MethodResolver> methodResolvers;

    public Verifier(URI location, final SignatureSuiteProvider suiteProvider) {
        this.location = location;
        this.document = null;
        
        this.suiteProvider = suiteProvider;
        
        this.methodResolvers = defaultResolvers();
        this.params = new LinkedHashMap<>(10);
    }

    public Verifier(JsonObject document, final SignatureSuiteProvider suiteProvider) {
        this.document = document;
        this.location = null;
        
        this.suiteProvider = suiteProvider;

        this.methodResolvers = defaultResolvers();
        this.params = new LinkedHashMap<>(10);
    }

    public static final Collection<MethodResolver> defaultResolvers() {
        Collection<MethodResolver> resolvers = new LinkedHashSet<>();
        resolvers.add(new DidUrlMethodResolver());
        resolvers.add(new HttpMethodResolver());
        return resolvers;
    }

    public Verifier methodResolvers(Collection<MethodResolver> resolvers) {
        this.methodResolvers = resolvers;
        return this;
    }

    /**
     * Custom verifier parameters that can be consumed during validation.
     * 
     * @param name a name of the parameter
     * @param value a value of the parameter
     *
     * @return the verifier instance
     */
    public Verifier param(final String name, final Object value) {
        params.put(name, value);
        return this;
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @return {@link Verifiable} object representing the verified credentials or a presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable isValid() throws VerificationError, DocumentError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }

        if (document != null) {
            return verify(document);
        }

        if (location != null) {
            return verify(location);
        }

        throw new IllegalStateException();
    }

    private Verifiable verify(final URI location) throws VerificationError, DocumentError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).base(base).get();

            return verifyExpanded(expanded);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private Verifiable verify(final JsonObject document) throws VerificationError, DocumentError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            return verifyExpanded(expanded);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private Verifiable verifyExpanded(JsonArray expanded) throws VerificationError, DocumentError {

        if (expanded == null || expanded.isEmpty() || expanded.size() > 1) {
            throw new DocumentError(ErrorType.Invalid);
        }
        
        final JsonValue verifiable = expanded.iterator().next();

        if (JsonUtils.isNotObject(verifiable)) {
            throw new DocumentError(ErrorType.Invalid);
        }

        return verifyExpanded(verifiable.asJsonObject());
    }

    private Verifiable verifyExpanded(final JsonObject expanded) throws VerificationError, DocumentError {

        // get a verifiable representation
        final Verifiable verifiable = get(expanded);

        if (verifiable.isCredential()) {

            // data integrity and metadata validation
            validate(verifiable.asCredential());

            verifyProofs(expanded);
            
            return verifiable;

        } else if (verifiable.isPresentation()) {

            // verify presentation proofs
            verifyProofs(expanded);
            
            // verify embedded credentials
            for (final Credential credential : verifiable.asPresentation().getCredentials()) {

                if (!credential.isCredential()) {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS, LdTerm.TYPE);
                }

                // data integrity and metadata validation
                validate(credential);

                verifyProofs(credential.asExpandedJsonLd());
            }
            
            return verifiable;
        } 
        throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
    }

    private Collection<Proof> verifyProofs(JsonObject expanded) throws VerificationError, DocumentError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonValue> expandedProofs = EmbeddedProof.assertProof(expanded);

        // a data before issuance - no proof attached
        final JsonObject data = EmbeddedProof.removeProof(expanded);

        final Collection<Proof> proofs = new ArrayList<>(expandedProofs.size()); 
        
        // read attached proofs
        for (final JsonValue expandedProof : expandedProofs) {

            if (JsonUtils.isNotObject(expandedProof)) {
                throw new DocumentError(ErrorType.Invalid, VcVocab.PROOF);
            }

            final JsonObject proofObject = expandedProof.asJsonObject();

            final Collection<String> proofType = JsonLdReader.getType(proofObject);

            if (proofType == null || proofType.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, VcVocab.PROOF, LdTerm.TYPE);
            }

            final SignatureSuite signatureSuite = proofType.stream()
                    .filter(suiteProvider::isSupported)
                    .findFirst()
                    .map(suiteProvider::find)   //TODO ?!?
                    .orElseThrow(() -> new VerificationError(Code.UnsupportedCryptoSuite));

            final Proof proof = signatureSuite.readProof(proofObject);
            
            if (proof  == null) {
                throw new IllegalStateException("The suite [" + signatureSuite.id() + "] returns null as a proof.");
            }
            proofs.add(proof);
        }
        
        // sort the proofs in the verification order
        final ProofQueue queue = ProofQueue.create(proofs);
        
        // verify the proofs' signatures
        Proof proof = queue.pop();
        
        while (proof != null) {
        
            proof.validate(params);

            final byte[] proofValue = proof.getValue();

            
//            if (signatureSuite.getSchema() == null) {
//                throw new IllegalStateException("The suite [" + signatureSuite.getId() + "] does not provide proof schema.");
//            }
//
//            final LdProperty<byte[]> proofValueProperty = signatureSuite.getSchema().tagged(VcTag.ProofValue.name());
//
//            if (proofValueProperty == null) {
//                throw new IllegalStateException("The proof schema does not define the proof value.");
//            }

//            final LdObject proof = signatureSuite.getSchema().read(proofObject);

//            signatureSuite.getSchema().validate(proof, params);
                        
//            if (!proof.contains(proofValueProperty.term())) {
//                throw new DocumentError(ErrorType.Missing, proofValueProperty.term());
//            }
            
//            final byte[] proofValue = (byte[]) proof.value(proofValueProperty.term());

            if (proofValue == null || proofValue.length == 0) {
                throw new DocumentError(ErrorType.Missing, "ProofValue");
            }

//            final LdProperty<VerificationMethod> methodProperty = signatureSuite.getSchema().tagged(VcTag.VerificationMethod.name());
//
//            if (methodProperty == null) {
//                throw new IllegalStateException("The proof schema does not define a verification method.");
//            }

            VerificationMethod verificationMethod = getMethod(proof)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, "ProofVerificationMethod"));

            if (!(verificationMethod instanceof VerificationKey)) {
                throw new DocumentError(ErrorType.Unknown, "ProofVerificationMethod");
            }
            
            // remote a proof value
            proof.setValue(null);
            final JsonObject unsignedProof = proof.toJsonLd();
            
            // remote a proof value
//            final JsonObject unsignedProof = Json.createObjectBuilder(proofObject)
//                    .remove(proofValueProperty.term().uri())
//                    .build();

            final CryptoSuite cryptoSuite = proof.getCryptoSuite();
            
            if (cryptoSuite == null) {
                throw new VerificationError(Code.UnsupportedCryptoSuite);
            }
            
            final LinkedDataSignature signature = new LinkedDataSignature(cryptoSuite);

            // verify signature
            signature.verify(
                    data,
                    unsignedProof,
                    (VerificationKey) verificationMethod,
                    proofValue);
            
            proof = queue.pop();
        }
        // all good
        return proofs;
    }

    Optional<VerificationMethod> getMethod(final Proof proof) throws VerificationError, DocumentError {

        for (final VerificationMethod method : proof.getMethod()) {

            if (method == null) {
                throw new IllegalStateException(); // should never happen
            }

            final URI methodType = method.type();
            
            if (methodType != null 
                    && method instanceof VerificationKey 
                    && (((VerificationKey) method).publicKey() != null)
                    ) {
                return Optional.of(method);
            }

            return resolveMethod(method.id(), proof);
        }

        return Optional.empty();
    }
//
//    Optional<VerificationMethod> resolve(JsonObject method, SignatureSuite suite, LdProperty<VerificationMethod> property) throws DocumentError, VerificationError {
//        try {
//            URI id = JsonLdReader
//                    .getId(method)
//                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, property.term()));
//
//            return resolve(id, suite, property);
//
//        } catch (InvalidJsonLdValue e) {
//            throw new DocumentError(e, ErrorType.Invalid, property.term());
//        }
//    }

    Optional<VerificationMethod> resolveMethod(
            URI id, 
            Proof proof
            ) throws DocumentError {

        if (id == null) {
            throw new DocumentError(ErrorType.Missing, "ProofVerificationId");
        }

        // find the method id resolver
        final Optional<MethodResolver> resolver = methodResolvers.stream()
                .filter(r -> r.isAccepted(id))
                .findFirst();

        // try to resolve the method
        if (resolver.isPresent()) {
            return Optional.ofNullable(resolver.get().resolve(id, loader, proof.getSignatureSuite()));
        }

        throw new DocumentError(ErrorType.Unknown, "ProofVerificationId");
    }
    
    final void validate(final Credential credential) throws DocumentError, VerificationError {

        // validation
        if (credential.isExpired()
                || (credential.getValidUntil() != null
                        && credential.getValidUntil().isBefore(Instant.now()))
                ) {
            throw new VerificationError(Code.Expired);
        }

        if ((credential.getIssuanceDate() != null
                && credential.getIssuanceDate().isAfter(Instant.now()))

                || (credential.getIssued() != null
                        && credential.getIssued().isAfter(Instant.now()))

                || (credential.getValidFrom() != null
                        && credential.getValidFrom().isAfter(Instant.now()))) {

            throw new VerificationError(Code.NotValidYet);
        }
        validateData(credential);
    }
}
