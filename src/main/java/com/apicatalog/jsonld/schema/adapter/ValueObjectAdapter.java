package com.apicatalog.jsonld.schema.adapter;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.jsonld.schema.LdTerm;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
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

        if (type == null) {
            return value.asJsonObject().get(Keywords.VALUE);            
        }

        JsonValue valueType = value.asJsonObject().get(Keywords.TYPE);
        
        if (JsonUtils.isNotString(valueType)) {
            throw new IllegalArgumentException("The value object @type is not JSON string but [" + value.asJsonObject().get(Keywords.TYPE) + "].");            
        }

        if (type.uri().equalsIgnoreCase(((JsonString)valueType).getString())) {
          return value.asJsonObject().get(Keywords.VALUE);
        }
        
        throw new IllegalArgumentException("The value object @type is not [" + type.uri() +  "] but [" + value.asJsonObject().get(Keywords.TYPE) + "].");
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
