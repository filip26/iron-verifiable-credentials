package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.SigningError;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.proof.ProofOptions;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

public class SigningProcessor {

    private final URI location;
    private final JsonObject document;
    
    private final URI keyPairLocation;
    private final KeyPair keyPair;
    
    private final ProofOptions options;
    
    private DocumentLoader loader = null;
    
    protected SigningProcessor(URI location, URI keyPairLocation, ProofOptions options) {
        this.location = location;
        this.document = null;
        
        this.keyPairLocation = keyPairLocation;
        this.keyPair = null;
        
        this.options = options;
    }
    
    protected SigningProcessor(JsonObject document, KeyPair keyPair, ProofOptions options) {
        this.document = document;
        this.location = null;

        this.keyPair = keyPair;
        this.keyPairLocation = null;
        
        this.options = options;
    }
    
    public SigningProcessor loader(DocumentLoader loader) {
        this.loader = loader;
        return this;
    }
        
    /**
     * Get signed document in expanded form.
     * 
     * @return
     * @throws SigningError
     * @throws DataIntegrityError
     */
    public JsonObject get() throws SigningError, DataIntegrityError {
        
        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }
        
        //TODO make it configurable
        loader = new StaticContextLoader(loader);
        
        if (document != null && keyPair != null)  {
            return sign(document, keyPair, options);
        }
        
        if (location != null && keyPairLocation != null)  {
            return sign(location, keyPairLocation, options);
        }
        
        throw new IllegalStateException();
    }

    /**
     * Get signed document in compacted form.
     * 
     * @param context
     * @return
     * @throws SigningError
     * @throws DataIntegrityError
     */
    public JsonObject getCompacted(URI context)  throws SigningError, DataIntegrityError {
        
        final JsonObject signed = get();
        
        try {
            return JsonLd.compact(JsonDocument.of(signed), context).get();
        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(URI documentLocation, URI keyPairLocation, ProofOptions options) throws DataIntegrityError, SigningError {
        try {
            // load the document
            final JsonArray document = JsonLd.expand(documentLocation).loader(loader).get();

            // load key pair
            Document keys = loader.loadDocument(keyPairLocation, new DocumentLoaderOptions());

            // TODO keyPair type must match options.type
            Ed25519KeyPair2020 keyPair = Ed25519KeyPair2020.from(keys.getJsonContent().orElseThrow().asJsonObject()); // FIXME

            LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());

            JsonObject signed = signature.sign(document.getJsonObject(0), options, keyPair);

            return signed;

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(JsonObject document, KeyPair keyPair, ProofOptions options) throws DataIntegrityError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).get();

            // load key pair
            Document keys = loader.loadDocument(keyPairLocation, new DocumentLoaderOptions());

            LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());

            JsonObject signed = signature.sign(expanded.getJsonObject(0), options, keyPair);

            return signed;

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

}
