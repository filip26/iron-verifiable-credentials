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
import com.apicatalog.vc.StaticContextLoader;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class VerifierApi {

    private final URI location;
    private final JsonObject document;
    private DocumentLoader loader = null;

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

    public VerifierApi useBundledContexts(boolean buildedContexts) {
        //TOOD
        return this;
    }

    public boolean isValid() throws VerificationError, DataError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        //TODO make it configurable
        loader = new StaticContextLoader(loader);

        if (document != null) {
            return verify(document, loader);
        }

        if (location != null) {
            return verify(location, loader);
        }

        throw new IllegalStateException();
    }

    private static boolean verify(URI location, DocumentLoader loader) throws VerificationError, DataError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).get();

            return verifyExpanded(expanded, loader);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

    private static boolean verify(JsonObject document, DocumentLoader loader) throws VerificationError, DataError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).get();

            return verifyExpanded(expanded, loader);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

    private static boolean verifyExpanded(JsonArray expanded, DocumentLoader loader) throws DataError, VerificationError {

        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                return false;
            }
            boolean result = verifyExpanded(item.asJsonObject(), loader);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    private static boolean verifyExpanded(JsonObject expanded, DocumentLoader loader) throws DataError, VerificationError {

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
        
                    if (!signature.verify(data, proofObject, verificationMethod, proof.getValue())) {
                        return false;
                    }
        
                } catch (JsonLdError e) {
                    throw new VerificationError(e);
                }
            }
            return true;
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
