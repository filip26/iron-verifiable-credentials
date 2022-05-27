package com.apicatalog.vc.proof;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.lds.KeyPair;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class VerificationKeyReference implements VerificationMethod {

    private final URI id;
    private final DocumentLoader loader;
    
    public VerificationKeyReference(URI id, DocumentLoader loader) {
        this.id = id;
        this.loader = loader;
    }
    
    public URI getId() {
        return id;
    }
    
    @Override
    public KeyPair get() {
        
        try {
            Document document = loader.loadDocument(id, new DocumentLoaderOptions());
            
            //TODO check document content type
            //TODO document.getJsonContent().orElseThrow()
            JsonStructure json = document.getJsonContent().orElse(JsonObject.EMPTY_JSON_OBJECT);

            return KeyPair.from(json.asJsonObject());   //TODO check json type
            
        } catch (JsonLdError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //TODO
        return null;
    }
    
}
