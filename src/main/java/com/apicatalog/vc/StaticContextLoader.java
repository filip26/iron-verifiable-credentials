package com.apicatalog.vc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;

public class StaticContextLoader implements DocumentLoader {

    static Map<String, Document> staticCache = new HashMap<>();
    
    static {
        staticCache.put("https://www.w3.org/2018/credentials/examples/v1", get("2018-credentials-examples-v1.jsonld"));
        staticCache.put("https://www.w3.org/2018/credentials/v1", get("2018-credentials-v1.jsonld"));
        staticCache.put("https://w3id.org/security/suites/ed25519-2020/v1", get("security-suites-ed25519-2020-v1.jsonld"));
        staticCache.put("https://www.w3.org/ns/odrl.jsonld", get("odrl.jsonld"));
    }

    static JsonDocument get(String name) {
        try (final InputStream is = StaticContextLoader.class.getResourceAsStream(name)) {
            
            return JsonDocument.of(is);
                        
        } catch (IOException | JsonLdError e) {
            e.printStackTrace();
        }
        return  null;
    }
    
    DocumentLoader defaultLoader;
    
    public StaticContextLoader(DocumentLoader defaultLoader) {
        this.defaultLoader = defaultLoader;
    }
    
    @Override
    public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {

        if (staticCache.containsKey(url.toString())) {
            Document document = staticCache.get(url.toString());
            if (document != null) {
                return document;
            }
        }

        return defaultLoader.loadDocument(url, options);
    }

}
