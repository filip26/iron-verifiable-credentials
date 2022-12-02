package com.apicatalog.ld.schema.adapter;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.schema.LdTerm;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

public class ValueObjectAdapter implements LdValueAdapter<JsonValue, JsonValue> {

    protected final LdTerm type;

    public ValueObjectAdapter() {
        this(null);
    }

    public ValueObjectAdapter(LdTerm type) {
        this.type = type;
    }

    @Override
    public JsonValue read(JsonValue value) {

        if (!ValueObject.isValueObject(value)) {
            throw new IllegalArgumentException();
        }

        // TODO check type

        return value.asJsonObject().get(Keywords.VALUE);
    }

    @Override
    public JsonValue write(JsonValue value) {

        final JsonObjectBuilder builder = Json.createObjectBuilder();

        if (type != null) {
            builder.add(Keywords.TYPE, type.uri());
        }

        builder.add(Keywords.VALUE, value);

        return builder.build();
    }

}
