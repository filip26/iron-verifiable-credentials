package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.DataIntegrityError.Code;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.VerificationKey;
import com.apicatalog.lds.proof.EmbeddedProof;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class VerificationApi {

    private final URI location;
    private final JsonObject document;
    private DocumentLoader loader = null;
    
    protected VerificationApi(URI location) {
        this.location = location;
        this.document = null;
    }
    
    protected VerificationApi(JsonObject document) {
        this.document = document;
        this.location = null;
    }
    
    public VerificationApi loader(DocumentLoader loader) {
        this.loader = loader;
        return this;
    }
        
    public boolean verify() throws VerificationError, DataIntegrityError {
        
        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }
        
        if (document != null) {
            return verify(document, loader);
        }
        
        if (location != null) {
            return verify(location, loader);
        }
        
        throw new IllegalStateException();
    }

    private static boolean verify(URI location, DocumentLoader loader) throws VerificationError, DataIntegrityError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).get();

            return verifyExpanded(expanded, loader);
            
        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }
    
    private static boolean verify(JsonObject document, DocumentLoader loader) throws VerificationError, DataIntegrityError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).get();
            
            return verifyExpanded(expanded, loader);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }
    
    private static boolean verifyExpanded(JsonArray expanded, DocumentLoader loader) throws DataIntegrityError, VerificationError {
        //TODO validate each objects ?!
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

    private static boolean verifyExpanded(JsonObject expanded, DocumentLoader loader) throws DataIntegrityError, VerificationError {
        
        try {
            // data integrity check
            EmbeddedProof proof = EmbeddedProof.from(expanded, loader);

            // check proof type
            if (!Ed25519Signature2020.TYPE.equals(proof.getType())) {
                throw new DataIntegrityError(Code.UnknownCryptoSuiteType);
            }

            VerificationKey verificationMethod = get(proof.getVerificationMethod().getId(), loader);

            LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());

            return signature.verify(expanded, verificationMethod, proof.getValue());

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }
    
    // refresh/fetch verification method
    static final VerificationKey get(URI id, DocumentLoader loader) throws DataIntegrityError, JsonLdError {

        final Document document = loader.loadDocument(id, new DocumentLoaderOptions());

        JsonObject method = document.getJsonContent().orElseThrow().asJsonObject();

        // TODO check verification method type
        return Ed25519KeyPair2020.from(method);

    }
}
