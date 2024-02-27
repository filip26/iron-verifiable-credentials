package com.apicatalog.ld.signature.sd;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.BlankNode;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonPointer;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class DocumentSelector {

    protected final Collection<JsonPointer> pointers;

    protected DocumentSelector(Collection<JsonPointer> pointers) {
        this.pointers = pointers;
    }

    public static DocumentSelector of(Collection<String> pointers) {
        return new DocumentSelector(toJsonPointers(pointers));
    }

    public Map<String, JsonValue> getValues(JsonObject document) {

        final Map<String, JsonValue> map = new HashMap<>();

        for (JsonPointer pointer : pointers) {
            map.put(pointer.toString(), pointer.getValue(document));
        }

        return map;
    }

    public JsonObject getNodes(JsonObject document) {
        JsonObject root = null;

        for (final JsonPointer pointer : pointers) {
            root = createNode(root, document, pointer.toString().split("\\/"), 1);
        }

        final JsonObjectBuilder builder = Json.createObjectBuilder(root);
        for (Map.Entry<String, JsonValue> entry : root.entrySet()) {
            JsonValue value = denseArrays(entry.getValue());
            if (JsonUtils.isNotNull(value)) {
                builder.add(entry.getKey(), value);
            }
        }
        return builder.build();
    }

    static Collection<JsonPointer> toJsonPointers(Collection<String> pointers) {
        return pointers.stream()
                .sorted(Collections.reverseOrder())
                .map(Json::createPointer)
                .collect(Collectors.toList());
    }

    private static JsonObject createNode(JsonObject target, JsonObject source, String[] segments, int index) {

        if (index == segments.length) {
            return source;
        }

        JsonObjectBuilder node;

        if (JsonUtils.isNull(target)) {
            node = createNewNode(source);
        } else {
            node = Json.createObjectBuilder(target);
        }

        final JsonValue value = source.get(segments[index]);

        if (JsonUtils.isNull(value)) {
            throw new IllegalArgumentException();
        }

        if (JsonUtils.isScalar(value)) {
            node.add(segments[index], value);

        } else if (JsonUtils.isObject(value)) {
            node.add(segments[index], createNode(JsonUtils.isNotNull(target)
                    ? target.getJsonObject(segments[index])
                    : null,
                    value.asJsonObject(), segments, index + 1));

        } else if (JsonUtils.isArray(value)) {
            node.add(segments[index], createArray(JsonUtils.isNotNull(target)
                    ? target.getJsonArray(segments[index])
                    : null,
                    value.asJsonArray(), segments, index + 1));
        }

        return node.build();
    }

    private static JsonArray createArray(JsonArray target, JsonArray source, String[] segments, int index) {

        if (index == segments.length) {
            return source;
        }

        JsonArrayBuilder array;

        final int arrayIndex = Integer.parseInt(segments[index]);

        if (JsonUtils.isNull(target)) {

            array = Json.createArrayBuilder();
            for (int i = 0; i <= arrayIndex; i++) {
                array.add(JsonValue.NULL);
            }

        } else {
            array = Json.createArrayBuilder(target);

            // correct array size, should not happen as pointers are sorted
            if (arrayIndex >= target.size()) {
                for (int i = target.size(); i <= arrayIndex; i++) {
                    array.add(JsonValue.NULL);
                }
            }
        }

        final JsonValue value = source.get(arrayIndex);

        if (JsonUtils.isNull(value)) {
            throw new IllegalArgumentException();
        }

        if (JsonUtils.isScalar(value)) {
            array.set(arrayIndex, value);

        } else if (JsonUtils.isObject(value)) {
            array.set(arrayIndex, createNode(
                    getNode(target, arrayIndex),
                    value.asJsonObject(), segments, index + 1));

        } else if (JsonUtils.isArray(value)) {
            array.set(arrayIndex, createArray(
                    getArray(target, arrayIndex),
                    value.asJsonArray(), segments, index + 1));
        }

        return array.build();
    }

    private static JsonArray getArray(JsonArray source, int index) {
        JsonValue value = source != null ? source.get(index) : null;
        if (JsonUtils.isNull(value)) {
            return null;
        }
        return value.asJsonArray();
    }

    private static JsonObject getNode(JsonArray source, int index) {
        JsonValue value = source != null ? source.get(index) : null;
        if (JsonUtils.isNull(value)) {
            return null;
        }
        return value.asJsonObject();
    }

    private static JsonValue denseArrays(JsonValue input) {

        if (JsonUtils.isArray(input)) {
            JsonArrayBuilder builder = Json.createArrayBuilder();
            for (JsonValue item : input.asJsonArray()) {
                JsonValue value = denseArrays(item);
                if (JsonUtils.isNotNull(value)) {
                    builder.add(value);
                }
            }
            return builder.build();
        }
        if (JsonUtils.isObject(input)) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (Map.Entry<String, JsonValue> entry : input.asJsonObject().entrySet()) {
                builder.add(entry.getKey(), denseArrays(entry.getValue()));
            }
            return builder.build();
        }
        return input;
    }

    private static JsonObjectBuilder createNewNode(JsonObject source) {
        JsonObjectBuilder selection = Json.createObjectBuilder();

        final JsonValue id = source.get("id");

        if (JsonUtils.isString(id) && !BlankNode.hasPrefix(((JsonString) id).getString())) {
            selection.add("id", id);
        }

        final JsonValue type = source.get("type");
        if (JsonUtils.isNotNull(type)) {
            selection.add("type", type);
        }

        return selection;
    }
}
