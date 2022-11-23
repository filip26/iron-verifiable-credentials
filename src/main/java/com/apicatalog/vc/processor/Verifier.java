package com.apicatalog.vc.processor;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.SignatureSuiteProvider;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.json.EmbeddedProof;
import com.apicatalog.ld.signature.json.VerificationMethodJsonAdapter;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.ld.signature.proof.ProofProperty;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.apicatalog.vc.loader.StaticContextLoader;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class Verifier extends Processor<Verifier> {

    protected final SignatureSuiteProvider suiteProvider;

    private final URI location;
    private final JsonObject document;

    private String domain = null;
    
    private StatusVerifier statusVerifier = null;
    private SubjectVerifier subjectVerifier = null;
    
    private DidResolver didResolver = null;

    public Verifier(URI location, final SignatureSuiteProvider suiteProvider) {
        this.location = location;
        this.document = null;
        this.suiteProvider = suiteProvider;
    }

    public Verifier(JsonObject document, final SignatureSuiteProvider suiteProvider) {
        this.document = document;
        this.location = null;
        this.suiteProvider = suiteProvider;
    }

    /**
     * Set a credential status verifier. If not set then
     * <code>credentialStatus</code> is ignored if present.
     *
     * @param statusVerifier a custom status verifier instance
     * @return the verifier instance
     */
    public Verifier statusVerifier(StatusVerifier statusVerifier) {
        this.statusVerifier = statusVerifier;
        return this;
    }

    /**
     * Set a credential subject verifier. If not set then
     * <code>credentialStatus</code> is not verified.
     *
     * @param subjectVerifier a custom subject verifier instance
     * @return the verifier instance
     */
    public Verifier subjectVerifier(SubjectVerifier subjectVerifier) {
        this.subjectVerifier = subjectVerifier;
        return this;
    }
    
    public Verifier didResolver(final DidResolver didResolver) {
        this.didResolver = didResolver;
        return this;
    }

    public Verifier domain(final String domain) {
        this.domain = domain;
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
            throw new DocumentError(ErrorType.Invalid, "document", e);
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
            throw new DocumentError(ErrorType.Invalid, "document", e);
        }
    }

    private void verifyExpanded(JsonArray expanded) throws VerificationError, DocumentError {

        if (expanded == null || expanded.isEmpty()) {
            throw new DocumentError(ErrorType.Invalid, "document");
        }

        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                throw new DocumentError(ErrorType.Invalid, "document");
            }
            verifyExpanded(item.asJsonObject());
        }
    }

    private void verifyExpanded(final JsonObject expanded) throws VerificationError, DocumentError {

        // get a verifiable representation
        final Verifiable veri1fiable = get(expanded);

        if (veri1fiable.isCredential()) {

            // data integrity and metadata validation
            validate(veri1fiable.asCredential(), statusVerifier, subjectVerifier);

            verifyProofs(expanded);

        } else if (veri1fiable.isPresentation()) {

            // verify embedded credentials
            for (final JsonObject expandedCredential : veri1fiable.asPresentation().getCredentials()) {
                
                final Verifiable credential = get(expandedCredential);
                
                if (!credential.isCredential()) {
                    throw new DocumentError(ErrorType.Invalid, "CredentialType");
                }

                // data integrity and metadata validation
                validate(credential.asCredential(), statusVerifier, subjectVerifier);

                verifyProofs(expandedCredential);
            }

            verifyProofs(expanded);
        }

        throw new DocumentError(ErrorType.Unknown, Keywords.TYPE);
    }

    private void verifyProofs(JsonObject expanded) throws VerificationError, DocumentError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonValue> proofs = EmbeddedProof.assertProof(expanded);

        // a data before issuance - no proof attached
        final JsonObject data = EmbeddedProof.removeProof(expanded);

        // verify attached proofs' signatures
        for (final JsonValue proofValue : proofs) {

            if (JsonUtils.isNotObject(proofValue)) {
                throw new DocumentError(ErrorType.Invalid, "Proof");
            }

            final Collection<String> proofType = JsonLdUtils.getType(proofValue.asJsonObject());

            if (proofType == null || proofType.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, "ProofType");
            }

            final SignatureSuite signatureSuite = proofType.stream()
                    .filter(suiteProvider::isSupported).findFirst()
                    .map(suiteProvider::getSignatureSuite)
                    .orElseThrow(() -> new VerificationError(Code.UnknownCryptoSuite));

            final Proof proof = signatureSuite.getProofAdapter()
                    .deserialize(proofValue.asJsonObject());

            validate(proof);

            final VerificationMethod verificationMethod = getMethod(proof.getMethod().id(),
                    loader, signatureSuite.getProofAdapter().getMethodAdapter());

            if (!(verificationMethod instanceof VerificationKey)) {
                throw new VerificationError(Code.UnknownVerificationMethod);
            }

            final LinkedDataSignature signature = new LinkedDataSignature(signatureSuite);

            // verify signature
            signature.verify(data, proofValue.asJsonObject(), (VerificationKey) verificationMethod,
                    proof.getValue());
        }
        // all good
    }

    // refresh/fetch verification method
    final VerificationMethod getMethod(final URI id, final DocumentLoader loader, VerificationMethodJsonAdapter keyAdapter) throws DocumentError, VerificationError {

        if (DidUrl.isDidUrl(id)) {

            DidResolver resolver = didResolver;

            if (resolver == null) {
                resolver = new DidKeyResolver();
            }

            final DidDocument didDocument = resolver.resolve(DidUrl.from(id));

            return didDocument.verificationMethod().stream()
                    .filter(vm -> keyAdapter.isSupportedType(vm.type()))
                    .map(did -> new VerificationKeyImpl(
                            did.id().toUri(),
                            did.controller().toUri(),
                            did.type(),
                            did.publicKey()))
                    .findFirst().orElseThrow(() -> new VerificationError(Code.UnknownVerificationKey));
        }

        try {
            final JsonArray document = JsonLd.expand(id)
                                            .loader(loader)
                                            .context(keyAdapter.getContextFor(id)) // an optional expansion context
                                            .get();

            for (final JsonValue method : document) {

                if (JsonUtils.isNotObject(method)) {
                    continue;
                }

                // take the first method matching type
                if (JsonLdUtils
                        .getType(method.asJsonObject())
                        .stream()
                        .anyMatch(m -> keyAdapter.isSupportedType(m))) {

                    return keyAdapter.deserialize(method.asJsonObject());
                }
            }

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(ErrorType.Invalid, "document", e);
        }

        throw new VerificationError(Code.UnknownVerificationKey);
    }

    private static final void validate(final Credential credential, final StatusVerifier statusVerifier, final SubjectVerifier subjectVerifier)
            throws DocumentError, VerificationError {

        // data integrity - issuance date is a mandatory property
        if (credential.getIssuanceDate() == null
                && credential.getValidFrom() == null 
                && credential.getIssued() == null) {
            throw new DocumentError(ErrorType.Missing, Credential.ISSUANCE_DATE);
        }

        // validation
        if (credential.isExpired()) {
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

        // status check
        if (statusVerifier != null && credential.getStatus() != null) {
            statusVerifier.verify(credential.getStatus());
        }

        // subject check
        if (subjectVerifier != null) {
            subjectVerifier.verify(credential.getSubject());
        }
    }

    private final void validate(final Proof proof) throws VerificationError, DocumentError {

        // purpose
        if (proof.getPurpose() == null) {
            throw new DocumentError(ErrorType.Missing, ProofProperty.Purpose);
        }

        // verification method
        if (proof.getMethod() == null) {
            throw new DocumentError(ErrorType.Missing, ProofProperty.VerificationMethod);
        }

        // value
        if (proof.getValue() == null || proof.getValue().length == 0) {
            throw new DocumentError(ErrorType.Missing, ProofProperty.Value);
        }

        // created
        if (proof.getCreated() == null) {
            throw new DocumentError(ErrorType.Missing, ProofProperty.Created);
        }

        // domain
        if (StringUtils.isNotBlank(domain) && !domain.equals(proof.getDomain())) {
            throw new VerificationError(Code.InvalidProofDomain);
        }
    }

    class VerificationKeyImpl implements VerificationKey {

        final URI id;
        final String type;
        final URI controller;
        final byte[] publicKey;

        public VerificationKeyImpl(URI id, URI controller, String type, byte[] publicKey) {
            this.id = id;
            this.type = type;
            this.controller = controller;
            this.publicKey = publicKey;
        }

        @Override
        public URI id() {
            return id;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public URI controller() {
            return controller;
        }

        @Override
        public byte[] publicKey() {
            return publicKey;
        }
    }
}
