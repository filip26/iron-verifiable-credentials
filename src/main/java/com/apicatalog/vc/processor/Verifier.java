package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.schema.LdTerm;
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
import com.apicatalog.vc.VcTag;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.loader.StaticContextLoader;

import jakarta.json.Json;
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
     * @throws VerificationError
     * @throws DocumentError
     */
    public void isValid() throws VerificationError, DocumentError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }

        if (document != null) {
            verify(document);
            return;
        }

        if (location != null) {
            verify(location);
            return;
        }

        throw new IllegalStateException();
    }

    private void verify(final URI location) throws VerificationError, DocumentError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private void verify(final JsonObject document) throws VerificationError, DocumentError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private void verifyExpanded(JsonArray expanded) throws VerificationError, DocumentError {

        if (expanded == null || expanded.isEmpty()) {
            throw new DocumentError(ErrorType.Invalid);
        }

        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                throw new DocumentError(ErrorType.Invalid);
            }
            verifyExpanded(item.asJsonObject());
        }
    }

    private void verifyExpanded(final JsonObject expanded) throws VerificationError, DocumentError {

        // get a verifiable representation
        final Verifiable veri1fiable = get(expanded);

        if (veri1fiable.isCredential()) {

            // data integrity and metadata validation
            validate(veri1fiable.asCredential());

            verifyProofs(expanded);

        } else if (veri1fiable.isPresentation()) {

            // verify presentation proofs
            verifyProofs(expanded);
            
            // verify embedded credentials
            for (final JsonObject expandedCredential : veri1fiable.asPresentation().getCredentials()) {

                final Verifiable credential = get(expandedCredential);

                if (!credential.isCredential()) {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS, LdTerm.TYPE);
                }

                // data integrity and metadata validation
                validate(credential.asCredential());

                verifyProofs(expandedCredential);
            }

        } else {
            throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
        }
    }

    private void verifyProofs(JsonObject expanded) throws VerificationError, DocumentError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonValue> proofs = EmbeddedProof.assertProof(expanded);

        // a data before issuance - no proof attached
        final JsonObject data = EmbeddedProof.removeProof(expanded);

        // verify attached proofs' signatures
        for (final JsonValue embeddedProof : proofs) {

            if (JsonUtils.isNotObject(embeddedProof)) {
                throw new DocumentError(ErrorType.Invalid, VcVocab.PROOF);
            }

            final JsonObject proofObject = embeddedProof.asJsonObject();

            final Collection<String> proofType = JsonLdReader.getType(proofObject);

            if (proofType == null || proofType.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, VcVocab.PROOF, LdTerm.TYPE);
            }

            final SignatureSuite signatureSuite = proofType.stream()
                    .filter(suiteProvider::isSupported)
                    .findFirst()
                    .map(suiteProvider::find)
                    .orElseThrow(() -> new VerificationError(Code.UnsupportedCryptoSuite));

            if (signatureSuite.getSchema() == null) {
                throw new IllegalStateException("The suite [" + signatureSuite.getId() + "] does not provide proof schema.");
            }

            final LdProperty<byte[]> proofValueProperty = signatureSuite.getSchema().tagged(VcTag.ProofValue.name());

            if (proofValueProperty == null) {
                throw new IllegalStateException("The proof schema does not define the proof value.");
            }

            final LdObject proof = signatureSuite.getSchema().read(proofObject);

            signatureSuite.getSchema().validate(proof, params);

            if (!proof.contains(proofValueProperty.term())) {
                throw new DocumentError(ErrorType.Missing, proofValueProperty.term());
            }

            final byte[] proofValue = (byte[]) proof.value(proofValueProperty.term());

            if (proofValue == null || proofValue.length == 0) {
                throw new DocumentError(ErrorType.Missing, proofValueProperty.term());
            }

            final LdProperty<VerificationMethod> methodProperty = signatureSuite.getSchema().tagged(VcTag.VerificationMethod.name());

            if (methodProperty == null) {
                throw new IllegalStateException("The proof schema does not define a verification method.");
            }

            VerificationMethod verificationMethod = getMethod(methodProperty, proofObject, signatureSuite)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, methodProperty.term()));

            if (!(verificationMethod instanceof VerificationKey)) {
                throw new DocumentError(ErrorType.Unknown, methodProperty.term());
            }

            // remote a proof value
            final JsonObject unsignedProof = Json.createObjectBuilder(proofObject)
                    .remove(proofValueProperty.term().uri())
                    .build();

            final LinkedDataSignature signature = new LinkedDataSignature(signatureSuite.getCryptoSuite());

            // verify signature
            signature.verify(
                    data,
                    unsignedProof,
                    (VerificationKey) verificationMethod,
                    proofValue);
        }
        // all good
    }

    Optional<VerificationMethod> getMethod(final LdProperty<VerificationMethod> property, final JsonObject proofObject, final SignatureSuite suite) throws VerificationError, DocumentError {

        final JsonArray expanded = proofObject.getJsonArray(property.term().uri());

        if (JsonUtils.isNull(expanded) || expanded.isEmpty()) {
            return Optional.empty();
        }

        for (final JsonValue methodValue : expanded) {

            if (JsonUtils.isNotObject(methodValue)) {
                throw new IllegalStateException(); // should never happen
            }

            final JsonObject methodObject = methodValue.asJsonObject();

            final Collection<String> types = JsonLdReader.getType(methodObject);

            if (types == null || types.isEmpty()) {
                return resolve(methodObject, suite, property);
            }

            final VerificationMethod method = property.read(methodObject);

            if (method instanceof VerificationKey && (((VerificationKey) method).publicKey() != null)) {
                return Optional.of(method);
            }

            return resolve(method.id(), suite, property);
        }

        return Optional.empty();
    }

    Optional<VerificationMethod> resolve(JsonObject method, SignatureSuite suite, LdProperty<VerificationMethod> property) throws DocumentError, VerificationError {
        try {
            URI id = JsonLdReader
                    .getId(method)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, property.term()));

            return resolve(id, suite, property);

        } catch (InvalidJsonLdValue e) {
            throw new DocumentError(e, ErrorType.Invalid, property.term());
        }

    }

    Optional<VerificationMethod> resolve(URI id, SignatureSuite suite, LdProperty<VerificationMethod> property) throws DocumentError {

        if (id == null) {
            throw new DocumentError(ErrorType.Invalid, property.term());
        }

        // find the method id resolver
        final Optional<MethodResolver> resolver = methodResolvers.stream()
                .filter(r -> r.isAccepted(id))
                .findFirst();

        // try to resolve the method
        if (resolver.isPresent()) {
            return Optional.ofNullable(resolver.get().resolve(id, loader, suite));
        }

        throw new DocumentError(ErrorType.Unknown, property.term());
    }
    
    final void validate(final Credential credential) throws DocumentError, VerificationError {

        // validation
        if (credential.isExpired()) {
            throw new VerificationError(Code.Expired);
        }

        if ((credential.getIssuanceDate() != null
                && (credential.getIssuanceDate().getTime() > (new Date()).getTime()))

                || (credential.getIssued() != null
                        && credential.getIssued().getTime() > (new Date()).getTime())

                || (credential.getValidFrom() != null
                        && credential.getValidFrom().getTime() > (new Date()).getTime())) {

            throw new VerificationError(Code.NotValidYet);
        }
        validateData(credential);
    }
}
