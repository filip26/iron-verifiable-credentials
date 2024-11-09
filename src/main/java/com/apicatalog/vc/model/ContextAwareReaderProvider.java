package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonObject;

public class ContextAwareReaderProvider implements VerifiableReader {

    protected final Map<String, VerifiableReader> readers;

    protected ContextAwareReaderProvider(Map<String, VerifiableReader> readers) {
        this.readers = readers;
    }

    public ContextAwareReaderProvider() {
        this(new HashMap<>());
    }
    
    public ContextAwareReaderProvider with(String context, VerifiableReader reader) {
        readers.put(context, reader);
        return this;
    }

    @Override
    public Verifiable read(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {

        // extract context
        final Collection<String> contexts;

        try {
            contexts = JsonLdContext.strings(document);
        } catch (IllegalArgumentException e) {
            throw new DocumentError(e, ErrorType.Invalid, "Context");
        }

        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        final String firstContext = contexts.iterator().next();

        VerifiableReader reader = readers.get(firstContext);

        if (reader != null) {
            return reader.read(document, loader, base);
        }

        return null;
    }
}
