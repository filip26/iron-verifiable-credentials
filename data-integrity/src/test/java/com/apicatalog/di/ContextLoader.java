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
            "https://www.w3.org/2018/credentials/v1",
            load(SecurityContexts.contextAsStream("https://www.w3.org/2018/credentials/v1")),

            "https://www.w3.org/ns/credentials/v2",
            load(SecurityContexts.contextAsStream("https://www.w3.org/ns/credentials/v2")),

            "https://w3id.org/security/suites/ed25519-2020/v1",
            load(SecurityContexts.contextAsStream("https://w3id.org/security/suites/ed25519-2020/v1")),

            "https://www.w3.org/ns/credentials/examples/v2",
            load("examples-v2.json"),

            "https://w3id.org/citizenship/v4rc1",
            load("citizenship-v4rc1.json"));

    @Override
    public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {
        return CONTEXTS.get(url.toString());
    }

    public static DocumentLoader getInstance() {
        return new ContextLoader();
    }

    static Document load(String name) {
        return load(Resources.class.getResourceAsStream("context/" + name));
    }

    static Document load(InputStream is) {
        try {
            return JsonDocument.of(is);
        } catch (JsonLdError e) {
            throw new IllegalStateException(e);
        }
    }
}
