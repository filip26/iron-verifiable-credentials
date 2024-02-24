package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.method.resolver.DidUrlMethodResolver;
import com.apicatalog.vc.method.resolver.HttpMethodResolver;
import com.apicatalog.vc.method.resolver.MethodResolver;
import com.apicatalog.vc.proof.EmbeddedProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public class Verifier2 extends Processor<Verifier2> {

    private final SignatureSuite[] suites;

    private final URI location;
    private final JsonObject document;

    private final Map<String, Object> params;

    private Collection<MethodResolver> methodResolvers;

    protected Verifier2(URI location, final SignatureSuite... suites) {
        this.location = location;
        this.document = null;

        this.suites = suites;

        this.methodResolvers = defaultResolvers();
        this.params = new LinkedHashMap<>(10);
    }

    public Verifier2(JsonObject document, final SignatureSuite... suites) {
        this.document = document;
        this.location = null;

        this.suites = suites;

        this.methodResolvers = defaultResolvers();
        this.params = new LinkedHashMap<>(10);
    }

    public static final Collection<MethodResolver> defaultResolvers() {
        Collection<MethodResolver> resolvers = new LinkedHashSet<>();
        resolvers.add(new DidUrlMethodResolver(MultibaseDecoder.getInstance(), MulticodecDecoder.getInstance(Tag.Key)));
        resolvers.add(new HttpMethodResolver());
        return resolvers;
    }

    //TODO resolvers should be multilevel, per verifier, per proof type, e.g. DidUrlMethodResolver could be different.
    public Verifier2 methodResolvers(Collection<MethodResolver> resolvers) {
        this.methodResolvers = resolvers;
        return this;
    }

    /**
     * Custom verifier parameters that can be consumed during validation.
     * 
     * @param name  a name of the parameter
     * @param value a value of the parameter
     *
     * @return the verifier instance
     */
    public Verifier2 param(final String name, final Object value) {
        if (value != null) {
            params.put(name, value);

        } else if (params.containsKey(name)) {
            params.remove(name);
        }
        return this;
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable isValid() throws VerificationError, DocumentError {

        if (document != null) {
            return verify(document, getLoader());
        }

        if (location != null) {
            return verify(location, getLoader());
        }

        throw new IllegalStateException();
    }

    protected DocumentLoader getLoader() {

        DocumentLoader loader = defaultLoader;
        
        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }        
        return loader;
    }
    
    protected Verifiable verify(final URI location, DocumentLoader loader) throws VerificationError, DocumentError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return verify(json.asJsonObject(), loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable verify(final JsonObject document, DocumentLoader loader) throws VerificationError, DocumentError {

        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            return verifyExpanded(Verifiable.getVersion(document), expanded, loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private Verifiable verifyExpanded(final ModelVersion version, JsonArray expanded, DocumentLoader loader) throws VerificationError, DocumentError {

        if (expanded == null || expanded.isEmpty() || expanded.size() > 1) {
            throw new DocumentError(ErrorType.Invalid);
        }

        final JsonValue verifiable = expanded.iterator().next();

        if (JsonUtils.isNotObject(verifiable)) {
            throw new DocumentError(ErrorType.Invalid);
        }

        return verifyExpanded(version, verifiable.asJsonObject(), loader);
    }

    private Verifiable verifyExpanded(final ModelVersion version, final JsonObject expanded, DocumentLoader loader) throws VerificationError, DocumentError {

        // get a verifiable representation
        final Verifiable verifiable = Verifiable.of(version, expanded);

        if (verifiable.isCredential()) {

            // data integrity and metadata validation
            validate(verifiable.asCredential());

            verifiable.proofs(verifyProofs(expanded, loader));

            return verifiable;

        } else if (verifiable.isPresentation()) {

            // verify presentation proofs
            verifiable.proofs(verifyProofs(expanded, loader));

            final Collection<Credential> credentials = new ArrayList<>();

            for (final JsonObject presentedCredentials : Presentation.getCredentials(expanded)) {

                if (!Credential.isCredential(presentedCredentials)) {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS, Term.TYPE);
                }

                credentials.add(verifyExpanded(version, presentedCredentials, loader).asCredential());
            }

            verifiable.asPresentation().credentials(credentials);

            return verifiable;
        }
        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    protected Collection<Proof> verifyProofs(JsonObject expanded, DocumentLoader loader) throws VerificationError, DocumentError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonObject> expandedProofs = EmbeddedProof.assertProof(expanded);

        // a data before issuance - no proof attached
        final JsonObject unsigned = EmbeddedProof.removeProofs(expanded);

        final Collection<Proof> proofs = new ArrayList<>(expandedProofs.size());

        // read attached proofs
        for (final JsonObject expandedProof : expandedProofs) {

            final Collection<String> proofTypes = LdType.strings(expandedProof);

            if (proofTypes == null || proofTypes.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, VcVocab.PROOF, Term.TYPE);
            }

            final SignatureSuite signatureSuite = findSuite(proofTypes, expandedProof);

            if (signatureSuite == null) {
                throw new VerificationError(Code.UnsupportedCryptoSuite);
            }

            final Proof proof = signatureSuite.getProof(expandedProof);

            if (proof == null) {
                throw new IllegalStateException("The suite [" + signatureSuite + "] returns null as a proof.");
            }
            proofs.add(proof);
        }

        // sort the proofs in the verification order
        final ProofQueue queue = ProofQueue.create(proofs);

        // verify the proofs' signatures
        Proof proof = queue.pop();

        while (proof != null) {

            proof.validate(params);
            
            final ProofValue proofValue = proof.signature();

            if (proofValue == null) {
                throw new DocumentError(ErrorType.Missing, "ProofValue");
            }

            VerificationMethod verificationMethod = getMethod(proof, loader)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, "ProofVerificationMethod"));

            if (!(verificationMethod instanceof VerificationKey)) {
                throw new DocumentError(ErrorType.Unknown, "ProofVerificationMethod");
            }
            
            proof.verify(unsigned, (VerificationKey) verificationMethod);

            proof = queue.pop();
        }
        // all good
        return proofs;
    }

    Optional<VerificationMethod> getMethod(final Proof proof, DocumentLoader loader) throws VerificationError, DocumentError {

        final VerificationMethod method = proof.method();

        if (method == null) {
            throw new DocumentError(ErrorType.Missing, "ProofVerificationMethod");
        }

        final URI methodType = method.type();

        if (methodType != null
                && method instanceof VerificationKey
                && (((VerificationKey) method).publicKey() != null)) {
            return Optional.of(method);
        }

        return resolveMethod(method.id(), proof, loader);
    }

    Optional<VerificationMethod> resolveMethod(
            URI id,
            Proof proof,
            DocumentLoader loader) throws DocumentError {

        if (id == null) {
            throw new DocumentError(ErrorType.Missing, "ProofVerificationId");
        }

        // find the method id resolver
        final Optional<MethodResolver> resolver = methodResolvers.stream()
                .filter(r -> r.isAccepted(id))
                .findFirst();

        // try to resolve the method
        if (resolver.isPresent()) {
            return Optional.ofNullable(resolver.get().resolve(id, loader, proof));
        }

        throw new DocumentError(ErrorType.Unknown, "ProofVerificationId");
    }

    final void validate(final Credential credential) throws DocumentError, VerificationError {

        // validation
        if (credential.isExpired()) {
            throw new VerificationError(Code.Expired);
        }

        if (credential.isNotValidYet()) {
            throw new VerificationError(Code.NotValidYet);
        }

        validateData(credential);
    }

    SignatureSuite findSuite(Collection<String> proofTypes, JsonObject expandedProof) {
        for (final SignatureSuite suite : suites) {
            for (final String proofType : proofTypes) {
                if (suite.isSupported(proofType, expandedProof)) {
                    return suite;
                }
            }
        }
        return null;
    }
}
