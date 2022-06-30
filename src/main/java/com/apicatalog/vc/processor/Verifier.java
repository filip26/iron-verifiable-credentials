package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import com.apicatalog.did.DidDocument;
import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SignatureAdapter;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.EmbeddedProof;
import com.apicatalog.vc.loader.StaticContextLoader;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class Verifier extends Processor<Verifier> {

    private final URI location;
    private final JsonObject document;

    private String domain = null;
    private StatusVerifier statusVerifier = null;
    private DidResolver didResolver = null;

    public Verifier(URI location) {
        this.location = location;
        this.document = null;
    }

    public Verifier(JsonObject document) {
        this.document = document;
        this.location = null;
    }

    /**
     * Sets {@link CredentialStatus} verifier.
     * If not set then <code>credentialStatus</code> is not verified.
     *
     * @param statusVerifier
     * @return
     */
    public Verifier statusVerifier(StatusVerifier statusVerifier) {
        this.statusVerifier = statusVerifier;
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
     * Verifies VC/VP document. Throws VerificationError if the document is not valid or cannot be verified.
     * @throws VerificationError
     * @throws DataError
     */
    public void isValid() throws VerificationError, DataError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }

        if (signatureAdapter == null) {
            signatureAdapter = DEFAULT_SIGNATURE_ADAPTERS;
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

    private void verify(URI location) throws VerificationError, DataError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

    private void verify(JsonObject document) throws VerificationError, DataError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

    private void verifyExpanded(JsonArray expanded) throws VerificationError, DataError {

        if (expanded == null || expanded.isEmpty()) {
            throw new DataError(ErrorType.Invalid, "document");
        }

        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                throw new VerificationError(); //TODO code
            }
            verifyExpanded(item.asJsonObject());
        }
    }

    private void verifyExpanded(JsonObject expanded) throws VerificationError, DataError {

        // data integrity checks
        final Verifiable verifiable = get(expanded, false);

        // is expired?
        if (verifiable.isCredential() && verifiable.asCredential().isExpired()) {
            throw new VerificationError(Code.Expired);
        }

        // proof set
        if (EmbeddedProof.hasProof(expanded)) {

            final JsonObject data = EmbeddedProof.removeProof(expanded);

            final Collection<JsonValue> proofs = EmbeddedProof.getProof(expanded);

            if (proofs == null || proofs.size() == 0) {
                throw new DataError(ErrorType.Missing, "proof");
            }

            // verify attached proofs' signatures
            for (final JsonValue proofValue : proofs) {

                final Optional<EmbeddedProof> embeddedProof = signatureAdapter.materializeProof(proofValue, loader);

                // check proof type
                if (!embeddedProof.isPresent()) {

                    // @type property
                    if (!JsonLdUtils.hasType(proofValue)) {
                        throw new DataError(ErrorType.Missing, "proof", Keywords.TYPE);
                    }

                    throw new VerificationError(Code.UnknownCryptoSuite);
                }

                final EmbeddedProof proof = embeddedProof.get();

                // check domain
                if (StringUtils.isNotBlank(domain) && !domain.equals(proof.getDomain())) {
                    throw new VerificationError(Code.InvalidProofDomain);
                }

                final JsonObject proofObject = proofValue.asJsonObject();

                final SignatureSuite suite =
                        signatureAdapter
                            .getSuiteByType(proof.getType())
                            .orElseThrow(() -> new VerificationError(Code.UnknownCryptoSuite));

                final VerificationKey verificationMethod = get(proof.getVerificationMethod().getId(), suite.getAdapter());

                final LinkedDataSignature signature = new LinkedDataSignature(suite);

                // verify signature
                signature.verify(data, proofObject, verificationMethod, proof.getValue());

                // verify status
                if (statusVerifier != null && verifiable.isCredential()) {
                    statusVerifier.verify(verifiable.asCredential().getCredentialStatus());
                }

            }

            // all good
            return;
        }
        throw new DataError(ErrorType.Missing, "proof");
    }

    // refresh/fetch verification method
    final VerificationKey get(final URI id, final SignatureAdapter adapter) throws DataError, VerificationError {

        if (DidUrl.isDidUrl(id)) {

            DidResolver resolver = didResolver;

            if (resolver == null) {
                resolver = new DidKeyResolver();
            }

            final DidDocument didDocument = resolver.resolve(DidUrl.from(id));

            return didDocument
                        .getVerificationMethod()
                        .stream()
                        .filter(vm -> adapter.isSupportedType(vm.getType()))
                        .map(VerificationKey.class::cast)
                        .findAny()
                        .orElseThrow(IllegalStateException::new);
        }

        try {
            final JsonArray document = JsonLd
                                            .expand(id)
                                            .loader(loader)
                                            .context("https://w3id.org/security/suites/ed25519-2020/v1")
                                            .get();

            for (final JsonValue method : document) {

                final Optional<VerificationKey> key = adapter.materializeKey(method);

                // take the first key that match
                if (key.isPresent()) {
                    return key.get();
                }
            }

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }

        throw new VerificationError(Code.UnknownVerificationKey);
    }
}
