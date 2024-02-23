package com.apicatalog.vc.issuer;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.vc.model.Proof;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public class SignedCredentials extends Processor<SignedCredentials> {

    protected final JsonObject signed;
    protected final Proof draft;

    protected SignedCredentials(JsonObject signed, Proof draft) {
        this.signed = signed;
        this.draft = draft;
    }

    /**
     * Get signed document in expanded form.
     *
     * @return the signed document in expanded form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    public JsonObject getExpanded() {
        return signed;
    }

    /**
     * Get signed document in compacted form.
     *
     * @param contextLocation a context used to compact the document
     *
     * @return the signed document in compacted form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    public JsonObject getCompacted(final URI contextLocation) throws DocumentError {

        try {
            return postCompact(JsonLd.compact(JsonDocument.of(signed), contextLocation).loader(loader).get());

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    /**
     * Get signed document compacted using standard contexts.
     *
     * @return the signed document in compacted form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    public JsonObject getCompacted() throws DocumentError {
        return getCompacted(signed, getContext());
    }

    /**
     * Get signed document in compacted form.
     *
     * @param context a context or an array of contexts used to compact the document
     *
     * @return the signed document in compacted form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    JsonObject getCompacted(final JsonStructure context) throws SigningError, DocumentError {
        return getCompacted(signed, context);
    }

    JsonObject getCompacted(final JsonObject signed, final JsonStructure context) throws DocumentError {

        try {
            return postCompact(JsonLd
                    .compact(JsonDocument.of(signed), JsonDocument.of(context))
                    .loader(loader)
                    .get());

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    JsonObject postCompact(final JsonObject source) {

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

    final JsonArray getContext() {

        final Collection<String> urls = new HashSet<>();
        final JsonArrayBuilder contexts = Json.createArrayBuilder();

        // extract origin contexts
        if (signed != null && signed.containsKey(Keywords.CONTEXT)) {
            final JsonValue documentContext = signed.get(Keywords.CONTEXT);
            if (JsonUtils.isString(documentContext)) {
                urls.add(((JsonString) documentContext).getString());
                contexts.add(documentContext);

            } else if (JsonUtils.isObject(documentContext)) {
                contexts.add(documentContext);

            } else if (JsonUtils.isArray(documentContext)) {
                for (final JsonValue context : documentContext.asJsonArray()) {
                    if (JsonUtils.isString(context)) {
                        urls.add(((JsonString) context).getString());
                    }
                    contexts.add(context);
                }
            }
        }

        final Collection<String> provided = draft.getContext(modelVersion);

        if (provided != null) {
            // use .stream().filter(Predicate.not(urls::contains))
            for (String url : provided) {
                if (!urls.contains(url)) {
                    urls.add(url);
                    contexts.add(Json.createValue(url));
                }
            }
        }

        return contexts.build();
    }
}
