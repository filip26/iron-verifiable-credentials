package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
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
import com.apicatalog.vc.status.StatusPropertiesValidator;
import com.apicatalog.vc.status.StatusValidator;
import com.apicatalog.vc.subject.SubjectValidator;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public class Verifier {

    protected final SignatureSuite[] suites;

    protected DocumentLoader defaultLoader;
    protected boolean bundledContexts;
    protected URI base;

    protected StatusValidator statusValidator;
    protected SubjectValidator subjectValidator;

    protected ModelVersion modelVersion;

    protected Collection<MethodResolver> methodResolvers;

    protected Verifier(final SignatureSuite... suites) {
        this.suites = suites;

        // default values
        this.defaultLoader = null;
        this.bundledContexts = true;
        this.base = null;
        this.modelVersion = null;

        this.statusValidator = new StatusPropertiesValidator();
        this.subjectValidator = null;
        this.methodResolvers = defaultResolvers();
    }

    public static Verifier with(final SignatureSuite... suites) {
        return new Verifier(suites);
    }

    public Verifier loader(DocumentLoader loader) {
        this.defaultLoader = loader;
        return this;
    }

    /**
     * Use well-known contexts that are bundled with the library instead of fetching
     * it online. <code>true</code> by default. Disabling might cause slower
     * processing.
     *
     * @param enable
     * @return the processor instance
     */
    public Verifier useBundledContexts(boolean enable) {
        this.bundledContexts = enable;
        return this;
    }

    /**
     * If set, this overrides the input document's IRI.
     *
     * @param base
     * @return the processor instance
     */
    public Verifier base(URI base) {
        this.base = base;
        return this;
    }

    /**
     * Set a credential status verifier. If not set then
     * <code>credentialStatus</code> is ignored if present.
     *
     * @param statusValidator a custom status verifier instance
     * @return the verifier instance
     */
    public Verifier statusValidator(StatusValidator statusValidator) {
        this.statusValidator = statusValidator;
        return this;
    }

    /**
     * Set a credential subject verifier. If not set then
     * <code>credentialStatus</code> is not verified.
     *
     * @param subjectValidator a custom subject verifier instance
     * @return the verifier instance
     */
    public Verifier subjectValidator(SubjectValidator subjectValidator) {
        this.subjectValidator = subjectValidator;
        return this;
    }

    protected static final Collection<MethodResolver> defaultResolvers() {
        Collection<MethodResolver> resolvers = new LinkedHashSet<>();
        resolvers.add(new DidUrlMethodResolver(MultibaseDecoder.getInstance(), MulticodecDecoder.getInstance(Tag.Key)));
        resolvers.add(new HttpMethodResolver());
        return resolvers;
    }

    // TODO resolvers should be multilevel, per verifier, per proof type, e.g.
    // DidUrlMethodResolver could be different.
    public Verifier methodResolvers(Collection<MethodResolver> resolvers) {
        this.methodResolvers = resolvers;
        return this;
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param document
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable verify(JsonObject document) throws VerificationError, DocumentError {
        Objects.requireNonNull(document);
        return verify(document, null, getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param document
     * @param params
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable verify(JsonObject document, Map<String, Object> params) throws VerificationError, DocumentError {
        Objects.requireNonNull(document);
        return verify(document, params, getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param location
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable verify(URI location) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return verify(location, null, getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param location
     * @param params
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable verify(URI location, Map<String, Object> params) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return verify(location, params, getLoader());
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

    protected Verifiable verify(final URI location, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return verify(json.asJsonObject(), params, loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable verify(final JsonObject document, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {

        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            return verifyExpanded(Verifiable.getVersion(document), expanded, params, loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private Verifiable verifyExpanded(final ModelVersion version, JsonArray expanded, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {

        if (expanded == null || expanded.isEmpty() || expanded.size() > 1) {
            throw new DocumentError(ErrorType.Invalid);
        }

        final JsonValue verifiable = expanded.iterator().next();

        if (JsonUtils.isNotObject(verifiable)) {
            throw new DocumentError(ErrorType.Invalid);
        }

        return verifyExpanded(version, verifiable.asJsonObject(), params, loader);
    }

    private Verifiable verifyExpanded(final ModelVersion version, final JsonObject expanded, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {

        // get a verifiable representation
        final Verifiable verifiable = Verifiable.of(version, expanded);

        if (verifiable.isCredential()) {

            // data integrity and metadata validation
            validate(verifiable.asCredential());

            verifiable.proofs(verifyProofs(expanded, params, loader));

            return verifiable;

        } else if (verifiable.isPresentation()) {

            // verify presentation proofs
            verifiable.proofs(verifyProofs(expanded, params, loader));

            final Collection<Credential> credentials = new ArrayList<>();

            for (final JsonObject presentedCredentials : Presentation.getCredentials(expanded)) {

                if (!Credential.isCredential(presentedCredentials)) {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS, Term.TYPE);
                }

                credentials.add(verifyExpanded(version, presentedCredentials, params, loader).asCredential());
            }

            verifiable.asPresentation().credentials(credentials);

            return verifiable;
        }
        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    protected Collection<Proof> verifyProofs(JsonObject expanded, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {

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

            final Proof proof = signatureSuite.getProof(expandedProof, loader);

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

    protected void validateData(final Credential credential) throws DocumentError {

        // v1
        if ((credential.getVersion() == null || ModelVersion.V11.equals(credential.getVersion()))
                && credential.getIssuanceDate() == null) {
            // issuance date is a mandatory property
            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUANCE_DATE);
        }

        // status check
        if (statusValidator != null && JsonUtils.isNotNull(credential.getStatus())) {
            statusValidator.verify(credential.getStatus());
        }
    }
}
