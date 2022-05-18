package com.apicatalog.vc;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.document.RdfDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;

public class ClasspathLoader implements DocumentLoader {

    @Override
    public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {

        try (final InputStream is = getClass().getResourceAsStream(url.getSchemeSpecificPart())) {

            if (is == null) {
                throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED);
            }
            
            final Document document = toDocument(url, is);
            document.setDocumentUrl(url);

            return document;

        } catch (IOException e) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED);
        }
    }

    private static final Document toDocument(URI url, InputStream is) throws JsonLdError {

        if (url.toString().endsWith(".nq")) {
            return RdfDocument.of(is);
        }

        return JsonDocument.of(is);
    }

}
