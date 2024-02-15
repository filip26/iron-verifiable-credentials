package com.apicatalog.vc.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.vc.Vc;

public class StaticContextLoader implements DocumentLoader {

    protected static final Map<String, Document> staticCache = defaultValues();

    protected final DocumentLoader defaultLoader;

    public StaticContextLoader(final DocumentLoader defaultLoader) {
        this.defaultLoader = defaultLoader;
    }

    @Override
    public Document loadDocument(final URI url, final DocumentLoaderOptions options) throws JsonLdError {

        final Document document = staticCache.get(url.toString());

        if (document != null) {
            return document;
        }

        return defaultLoader.loadDocument(url, options);
    }

    public static Map<String, Document> defaultValues() {

        Map<String, Document> staticCache = new LinkedHashMap<>();

        staticCache.put("https://www.w3.org/2018/credentials/examples/v1", get("2018-credentials-examples-v1.jsonld"));
        staticCache.put("https://www.w3.org/2018/credentials/v1", get("2018-credentials-v1.jsonld"));
        staticCache.put("https://www.w3.org/ns/credentials/examples/v2", get("2023-credentials-examples-v2.jsonld"));
        staticCache.put("https://www.w3.org/ns/credentials/v2", get("2023-credentials-v2.jsonld"));
        staticCache.put("https://www.w3.org/ns/odrl.jsonld", get("odrl.jsonld"));
        staticCache.put("https://www.w3.org/ns/did/v1", get("did-v1.jsonld"));
        staticCache.put("https://w3id.org/security/data-integrity/v1", get("data-integrity-v1.jsonld"));
        staticCache.put("https://w3id.org/security/data-integrity/v2", get("data-integrity-v2.jsonld"));
        staticCache.put("https://w3id.org/security/multikey/v1", get("multikey-v1.jsonld"));

        return Collections.unmodifiableMap(staticCache);
    }

    protected static JsonDocument get(final String name) {
        try (final InputStream is = Vc.class.getResourceAsStream(name)) {
            return JsonDocument.of(is);

        } catch (IOException | JsonLdError e) {
            e.printStackTrace();
        }
        return null;
    }
}
