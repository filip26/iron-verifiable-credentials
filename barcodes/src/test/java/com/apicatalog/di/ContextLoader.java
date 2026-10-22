package com.apicatalog.di;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.security.vocab.SecurityContexts;

public class ContextLoader implements DocumentLoader {

    static final Map<String, Document> CONTEXTS = Map.of(
            SecurityContexts.VCDM_V2, context(SecurityContexts.VCDM_V2),
            "https://w3id.org/vc-barcodes/v1", resource("barcodes-v1.json"),
            "https://w3id.org/utopia/v2", resource("utopia-v2.json"));

    @Override
    public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {
        return CONTEXTS.get(url.toString());
    }

    public static DocumentLoader getInstance() {
        return new ContextLoader();
    }

    static Document resource(String name) {
        return document(Resources.class.getResourceAsStream("context/" + name));
    }

    static Document context(String uri) {
        return document(SecurityContexts.contextAsStream(uri));
    }

    static Document document(InputStream is) {
        try {
            return JsonDocument.of(is);
        } catch (JsonLdError e) {
            throw new IllegalStateException(e);
        }
    }
}
