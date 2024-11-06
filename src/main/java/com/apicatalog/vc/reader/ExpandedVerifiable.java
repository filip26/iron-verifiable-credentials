package com.apicatalog.vc.reader;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonStructure;

@Deprecated
public class ExpandedVerifiable {

    protected JsonStructure context;

    protected DocumentLoader loader;
    
    protected JsonObject expanded;

    public ExpandedVerifiable(JsonObject expanded, JsonStructure context, DocumentLoader loader) {
        this.expanded = expanded;
        this.loader = loader;
        this.context = context;
    }

    /**
     * Get a signed document in an expanded form.
     *
     * @return a signed document in an expanded form
     *
     * @throws DocumentError
     */
    public JsonObject expanded() {
        return expanded;
    }

    /**
     * Get a signed document compacted using standard contexts.
     *
     * @return the a signed document in compacted form
     *
     * @throws DocumentError
     */
    public JsonObject compacted() throws DocumentError {
        return compact(expanded(), context, loader);
    }

    /**
     * Get document in compacted form.
     *
     * @param customContext a context or an array of contexts used to compact the
     *                      document
     *
     * @return the document in compacted form
     *
     * @throws DocumentError
     */
    public JsonObject compacted(final JsonStructure customContext) throws DocumentError {
        return compact(expanded(), customContext, loader);
    }

    /**
     * Get document in compacted form.
     *
     * @param contextLocation a context used to compact the document
     *
     * @return the document in compacted form
     *
     * @throws DocumentError
     */
    public JsonObject compacted(final URI contextLocation) throws DocumentError {
        try {
            return postCompact(JsonLd.compact(JsonDocument.of(expanded()), contextLocation).loader(loader).get());

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    static JsonObject compact(final JsonObject signed, final JsonStructure context, final DocumentLoader loader) throws DocumentError {

        try {
            return postCompact(JsonLd
                    .compact(JsonDocument.of(signed), JsonDocument.of(context))
                    .loader(loader)
                    .get());

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    static JsonObject postCompact(final JsonObject source) {

        JsonObject compacted = source;

        // TODO use options
        // make sure @context is the first key and an array
        if (!compacted.keySet().iterator().next().equals(Keywords.CONTEXT)) {
            final JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add(Keywords.CONTEXT, JsonUtils.toJsonArray(compacted.get(Keywords.CONTEXT)));

            compacted.entrySet().stream()
                    .filter(entry -> !Keywords.CONTEXT.equals(entry.getKey()))
                    .forEach(entry -> builder.add(entry.getKey(), entry.getValue()));

            compacted = builder.build();
        }
        return compacted;
    }
}
