package com.apicatalog.oxygen.ld.json;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.oxygen.ld.LinkedData;
import com.apicatalog.oxygen.ld.LinkedNode;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JsonLdObject implements LinkedNode {

    protected URI id;
    protected Collection<String> types;
    protected Map<String, Collection<LinkedData>> values;

    protected JsonLdObject() {
        /* protected */
    }

    public static JsonLdObject of(JsonObject expanded) throws DocumentError {

        final JsonLdObject object = new JsonLdObject();

        object.id = getId(expanded);
        object.types = getTypes(expanded);
        object.values = expanded.entrySet()
                    .stream()
                    .filter(e -> !Keywords.ID.equals(e.getKey()) && !Keywords.TYPE.equals(e.getKey()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            v ->  JsonLdObject.of(v)
                        ));

        return object;
    }

    @Override
    public Collection<String> terms() {
        return values.keySet();
    }

    @Override
    public Collection<LinkedData> values(String term) {
        return values.getOrDefault(term, Collections.emptyList());
        
//        final JsonValue values = values.get(term);
//
//        if (JsonUtils.isArray(values)) {
//
//            final JsonArray array = values.asJsonArray();
//
//            if (values.isEmpty()) {
//                return Collections.emptySet();
//            }
//            
//            final JsonValue value = array.iterator().next();
//
//            if (JsonUtils.isNotObject(value) || ValueObject.isValueObject(value)) {
//                throw new DocumentError(ErrorType.Invalid, term);
//            }
//
//            return LdNodeImpl.of(value.asObject());
//
//        } else if (JsonUtils.isNotNull(values)) {
//            throw new DocumentError(ErrorType.Invalid, term);
//        }
//
//        return Collections.emptySet();
    }

    @Override
    public URI id() {
        return null;
    }

    @Override
    public Collection<String> type() {
        // TODO Auto-generated method stub
        return null;
    }

    public static final URI getId(JsonObject object) throws DocumentError {
        JsonValue id = object.get(Keywords.ID);

        if (JsonUtils.isString(id)) {
            try {
                return URI.create(((JsonString) id).getString());
            } catch (IllegalArgumentException e) {
                throw new DocumentError(ErrorType.Invalid, Keywords.ID);
            }
        } else if (JsonUtils.isNotNull(id)) {
            throw new DocumentError(ErrorType.Invalid, Keywords.ID);
        }
        return null;
    }

    public static final Collection<String> getTypes(JsonObject object) throws DocumentError {
        final JsonValue types = object.get(Keywords.TYPE);
        if (JsonUtils.isNonEmptyArray(types)) {
            final List<String> strings = new ArrayList<>(types.asJsonArray().size());
            for (final JsonValue type : types.asJsonArray()) {
                if (JsonUtils.isNotString(type)) {
                    throw new DocumentError(ErrorType.Invalid, Keywords.TYPE);
                }
                strings.add(((JsonString) type).getString());
            }
            return strings;
        }
        return Collections.emptyList();
    }

}
