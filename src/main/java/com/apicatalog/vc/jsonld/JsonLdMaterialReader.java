package com.apicatalog.vc.jsonld;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.api.ExpansionApi;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableMaterialReader;
import com.apicatalog.vc.model.generic.GenericMaterial;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JsonLdMaterialReader implements VerifiableMaterialReader {

    private static final Logger LOGGER = Logger.getLogger(JsonLdMaterialReader.class.getName());

    @Override
    public VerifiableMaterial read(
            final JsonObject document,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        Collection<String> context = context(document, Collections.emptyList());

        try {
            // expand the document
            final ExpansionApi expansion = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base);

            final JsonArray expanded = expansion.get();

            if (expanded != null && expanded.size() == 1) {

                final JsonValue item = expanded.iterator().next();

                if (JsonUtils.isObject(item)) {
                    return new GenericMaterial(
                            context,
                            document,
                            item.asJsonObject());
                }
            }

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
        throw new DocumentError(ErrorType.Invalid, "Document");
    }

    protected static Collection<String> context(final JsonObject object, Collection<String> defaultValue) {

        final JsonValue contexts = object.get(JsonLdKeyword.CONTEXT);

        if (JsonUtils.isNull(contexts)) {
            return defaultValue;
        }

        final Collection<JsonValue> items = JsonUtils.toCollection(contexts);

        if (items.isEmpty()) {
            return defaultValue;
        }

        final List<String> strings = new ArrayList<>(items.size());

        for (final JsonValue context : items) {

            if (JsonUtils.isString(context)) {
                final String contextUri = ((JsonString) context).getString();

                if (!isURI(contextUri)) {
                    throw new IllegalArgumentException("Invalid context value. Expected URI but got [" + context + "].");
                }
                strings.add(contextUri);

            } else {
                LOGGER.log(Level.WARNING, "Found an inline @context which integrity cannot be verified. Using inline @context in a production is considered a bad practice. {0}", context);
            }

        }
        return strings;
    }

    protected static final boolean isURI(final String value) {
        try {
            return URI.create(value) != null;
        } catch (IllegalArgumentException e) {
        }
        return false;
    }

}
