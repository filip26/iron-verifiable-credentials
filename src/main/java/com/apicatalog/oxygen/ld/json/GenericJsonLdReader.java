package com.apicatalog.oxygen.ld.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.oxygen.ld.LinkedData;
import com.apicatalog.oxygen.ld.LinkedLiteral;
import com.apicatalog.oxygen.ld.LinkedNode;
import com.apicatalog.oxygen.ld.reader.LinkedNodeAdapter;
import com.apicatalog.oxygen.ld.reader.LinkedDataReader;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class GenericJsonLdReader implements LinkedDataReader<LinkedData, JsonValue> {

    protected Collection<LinkedNodeAdapter<LinkedNode, JsonValue>> adapters;

    protected LinkedData current;

    @Override
    public Collection<LinkedData> read(JsonValue input) throws DocumentError {
        Objects.requireNonNull(input);
        return readExpanded(JsonUtils.toCollection(input));
    }

    protected Collection<LinkedData> readExpanded(Collection<JsonValue> items) throws DocumentError {

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        final Collection<LinkedData> results = new ArrayList<>(items.size());

        for (final JsonValue item : items) {
            results.add(readObject(item));
        }

        return results;
    }

    protected LinkedData readObject(JsonValue value) throws DocumentError {

        if (JsonUtils.isNotObject(value)) {
            throw new DocumentError(ErrorType.Invalid, "Document");
        }

        final JsonObject object = value.asJsonObject();

        return object.containsKey(Keywords.VALUE)
                ? readLiteral(object)
                : genericObject(object);
    }

    protected LinkedLiteral readLiteral(JsonObject value) {
        
        // for each adapter
        // adapter.read(value) == null contine;
        
        return JsonLdLiteral.of();
    }

    protected LinkedNode genericObject(JsonObject value) throws DocumentError {

        for (final Map.Entry<String, JsonValue> entry : value.entrySet()) {

            final Collection<LinkedData> propertyValue = read(entry.getValue());

        }
        return null;

    }
}
