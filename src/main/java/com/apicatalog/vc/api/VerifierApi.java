package com.apicatalog.vc.api;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
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

public final class VerifierApi {

    private final URI location;
    private final JsonObject document;
    private DocumentLoader loader = null;
    private StatusVerifier statusVerifier = null;
    private boolean bundledContexts = true;

    protected VerifierApi(URI location) {
        this.location = location;
        this.document = null;
    }

    protected VerifierApi(JsonObject document) {
        this.document = document;
        this.location = null;
    }

    public VerifierApi loader(DocumentLoader loader) {
        this.loader = loader;
        return this;
    }

    /**
     * Use well-known contexts that are bundled with the library instead of fetching it online. 
     * <code>true</code> by default. Disabling might cause slower processing.
     *  
     * @param enable
     * @return
     */
    public VerifierApi useBundledContexts(boolean enable) {
        this.bundledContexts = enable;
        return this;
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
            verify(document, loader, statusVerifier);
            return;
        }

        if (location != null) {
            verify(location, loader, statusVerifier);
            return;
        }

        throw new IllegalStateException();
    }

    private static void verify(URI location, DocumentLoader loader, StatusVerifier statusVerifier) throws VerificationError, DataError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).get();

            verifyExpanded(expanded, loader, statusVerifier);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

    private static void verify(JsonObject document, DocumentLoader loader, StatusVerifier statusVerifier) throws VerificationError, DataError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).get();

            verifyExpanded(expanded, loader, statusVerifier);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

    private static void verifyExpanded(JsonArray expanded, DocumentLoader loader, StatusVerifier statusVerifier) throws DataError, VerificationError {
        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                throw new VerificationError(); //TODO code
            }
            verifyExpanded(item.asJsonObject(), loader, statusVerifier);
        }
    }

    private static void verifyExpanded(JsonObject expanded, DocumentLoader loader, StatusVerifier statusVerifier) throws DataError, VerificationError {

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

                    VerificationKey verificationMethod = get(proof.getVerificationMethod().getId(), loader);

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
    static final VerificationKey get(URI id, DocumentLoader loader) throws DataError, JsonLdError {

        final JsonArray document = JsonLd.expand(id).loader(loader).get();

        JsonObject method = document.getJsonObject(0);  //FIXME

        // TODO check verification method type
        return Ed25519KeyPair2020.from(method);

    }
}
