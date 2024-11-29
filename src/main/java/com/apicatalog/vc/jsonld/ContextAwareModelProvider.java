package com.apicatalog.vc.jsonld;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.adapter.DocumentModelAdapter;
import com.apicatalog.vc.model.provider.ModelAdapterProvider;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class ContextAwareModelProvider implements ModelAdapterProvider {

    private static final Logger LOGGER = Logger.getLogger(ContextAwareModelProvider.class.getName());

    protected final Map<String, DocumentModelAdapter> readers;

    protected ContextAwareModelProvider(Map<String, DocumentModelAdapter> readers) {
        this.readers = readers;
    }

    public ContextAwareModelProvider() {
        this(new HashMap<>());
    }

    public ContextAwareModelProvider with(String context, DocumentModelAdapter reader) {
        readers.put(context, reader);
        return this;
    }
    

    @Override
    public DocumentModelAdapter reader(JsonObject document) throws DocumentError {
        // extract the first context as string value
        final String firstContext = firstContext(document);

        if (firstContext != null) {
            return readers.get(firstContext);
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
