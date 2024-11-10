package com.apicatalog.vc.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class ContextAwareReaderProvider implements VerifiableReader {

    private static final Logger LOGGER = Logger.getLogger(ContextAwareReaderProvider.class.getName());

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

        // extract the first context string value
        final String firstContext = firstContext(document);

        if (firstContext != null) {
            VerifiableReader reader = readers.get(firstContext);

            if (reader != null) {
                return reader.read(document, loader, base);
            }
        }
        return null;
    }

    protected static String firstContext(final JsonObject object) {

        final JsonValue contexts = object.get(JsonLdKeyword.CONTEXT);

        if (JsonUtils.isNull(contexts)) {
            return null;
        }

        if (JsonUtils.isString(contexts)) {
            return ((JsonString) contexts).getString();
        }

        if (JsonUtils.isArray(contexts) && !contexts.asJsonArray().isEmpty()) {
            JsonValue item = contexts.asJsonArray().iterator().next();
            if (JsonUtils.isString(item)) {
                return ((JsonString) item).getString();
            }
        }

        LOGGER.log(Level.WARNING, "Found an inline @context which is ignored. Using inline @context in a production is considered a bad practice. [{0}]", contexts);

        return null;
    }
}
