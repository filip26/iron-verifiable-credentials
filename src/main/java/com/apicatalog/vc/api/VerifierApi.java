package com.apicatalog.vc.api;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.lds.DataError;
import com.apicatalog.lds.DataError.ErrorType;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.VerificationError.Code;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.VerificationKey;
import com.apicatalog.lds.proof.EmbeddedProof;
import com.apicatalog.vc.CredentialStatus;
import com.apicatalog.vc.StaticContextLoader;
import com.apicatalog.vc.StatusVerifier;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class VerifierApi extends CommonApi {

    private final URI location;
    private final JsonObject document;
    private StatusVerifier statusVerifier = null;

    protected VerifierApi(URI location) {
        this.location = location;
        this.document = null;
    }

    protected VerifierApi(JsonObject document) {
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
    public VerifierApi statusVerifier(StatusVerifier statusVerifier) {
        this.statusVerifier = statusVerifier;
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
        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                throw new VerificationError(); //TODO code
            }
            verifyExpanded(item.asJsonObject());
        }
    }

    private void verifyExpanded(JsonObject expanded) throws VerificationError, DataError {

        // data integrity checks
        final Verifiable verifiable = Vc.get(expanded);

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

            for (final JsonValue proofValue : proofs) {

                final EmbeddedProof proof = EmbeddedProof.from(proofValue, loader);

                final JsonObject proofObject = proofValue.asJsonObject();

                try {
                    // check proof type
                    if (!Ed25519Signature2020.TYPE.equals(proof.getType())) {
                        throw new DataError(ErrorType.Unknown, "cryptoSuiteType");
                    }

                    VerificationKey verificationMethod = get(proof.getVerificationMethod().getId());

                    LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());

                    // verify signature
                    signature.verify(data, proofObject, verificationMethod, proof.getValue());

                    // verify status
                    if (statusVerifier != null && verifiable.isCredential()) {
                        statusVerifier.verify(verifiable.asCredential().getCredentialStatus());
                    }
                    
                } catch (JsonLdError e) {
                    throw new VerificationError(e);
                }
            }
            // all good
            return;
        }
        throw new DataError(ErrorType.Missing, "proof");
    }

    // refresh/fetch verification method
    final VerificationKey get(URI id) throws JsonLdError, DataError {

        final JsonArray document = JsonLd.expand(id).loader(loader).get();

        JsonObject method = document.getJsonObject(0);  //FIXME

        // TODO check verification method type
        return Ed25519KeyPair2020.from(method);

    }
}
