package com.apicatalog.jsonld;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.jsonld.uri.UriUtils;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JsonLdUtils {

    static final String XSD_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";

    protected JsonLdUtils() {}

    /**
     * Checks if the given {@link JsonValue} is {@link JsonObject} has the given type listed as one of its <code>@type</code> declarations.
     *
     * @param type
     * @param value
     * @return <code>true</code> if the given value is {@link JsonObject} and has property @type including the given type
     */
    public static boolean isTypeOf(final String type, final JsonValue value) {

        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("The 'type' parameter must not be null nor blank.");
        }

        if (value == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        return JsonUtils.isObject(value)
                && value.asJsonObject().containsKey(Keywords.TYPE)
                && JsonUtils
                    .toStream(value.asJsonObject().get(Keywords.TYPE))
                    .filter(JsonUtils::isString)
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .filter(StringUtils::isNotBlank)
                    .anyMatch(type::equals);
    }

    public static Collection<String> getType(final JsonObject value) {

        if (value == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        return JsonUtils
                    .toStream(value.get(Keywords.TYPE))
                    .filter(JsonUtils::isString)
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
    }

    public static boolean hasType(final JsonValue expanded) {

        if (expanded == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }

        return JsonUtils.isObject(expanded) && expanded.asJsonObject().containsKey(Keywords.TYPE);
    }

    public static boolean isXsdDateTime(JsonValue value) {

        if (value == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }

        return JsonUtils
                    .toStream(value)
                    .filter(JsonUtils::isObject)
                    .map(JsonValue::asJsonObject)
                    .map(o -> isTypeOf(XSD_DATE_TIME, o))
                    .findAny()
                    .orElse(false);
    }

    public static boolean hasPredicate(JsonObject subject, String predicate) {
        return JsonUtils.isNotNull(subject.get(predicate));
    }

    public static Collection<JsonValue> getObjects(JsonObject subject, String predicate) {

        JsonValue value = subject.get(predicate);

        if (JsonUtils.isNull(value)) {
            return Collections.emptyList();
        }

        if (JsonUtils.isArray(value) && value.asJsonArray().size() == 1) {
            value = value.asJsonArray().get(0);
        }

        if (JsonUtils.isObject(value) && value.asJsonObject().containsKey(Keywords.GRAPH) && value.asJsonObject().size() == 1) {
            value = value.asJsonObject().get(Keywords.GRAPH);
        }

        return JsonUtils.toCollection(value);
    }

    public static URI assertId(JsonValue subject, String base, String property) throws DataError {

        if (JsonUtils.isNotObject(subject) || !hasPredicate(subject.asJsonObject(), base + property)) {
            throw new DataError(ErrorType.Missing, property);
        }

        JsonValue value = JsonLdUtils
                                    .getObjects(subject.asJsonObject(), base + property)
                                    .stream()
                                    .findFirst()
                                    .orElseThrow(() -> new DataError(ErrorType.Missing, property));

        if (JsonUtils.isObject(value)) {
            value  = value.asJsonObject().get(Keywords.ID);
        }

        final String id;

        if (JsonUtils.isString(value)) {
            id = ((JsonString)value).getString();

        } else {
            throw new DataError(ErrorType.Invalid, property);
        }

        if (UriUtils.isURI(id)) {
            return URI.create(id);
        }

        throw new DataError(ErrorType.Invalid, property, Keywords.ID);
    }

    public static Instant assertXsdDateTime(JsonValue subject, String base, String property) throws DataError {

        if (JsonUtils.isNotObject(subject) || !hasPredicate(subject.asJsonObject(), base + property)) {
            throw new DataError(ErrorType.Missing, property);
        }

        final JsonValue value = JsonLdUtils
                                    .getObjects(subject.asJsonObject(), base + property)
                                    .stream()
                                    .findFirst()
                                    .orElseThrow(() -> new DataError(ErrorType.Missing, property, Keywords.VALUE));

        if (isXsdDateTime(value)) {
            return JsonUtils.toStream(value)
                    .filter(ValueObject::isValueObject)
                    .filter(item -> isTypeOf(XSD_DATE_TIME, item.asJsonObject()))
                    .map(ValueObject::getValue)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(JsonUtils::isString)
                    .findFirst()
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .map(datetimeValue -> {
                        try {
                            return Instant.parse(datetimeValue);

                        } catch (DateTimeParseException e) {
                            // invalid date time format
                        }
                        return null;
                    })
                    .orElseThrow(() ->  new DataError(ErrorType.Invalid, property, Keywords.VALUE));
        }
        throw new DataError(ErrorType.Invalid, property, Keywords.TYPE);
    }

    public static final Optional<URI> getId(JsonValue value) {

        if (value == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }

        return JsonLdUtils.findFirstObject(value)
                .map(o -> o.get(Keywords.ID))
                .filter(JsonUtils::isString)
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .map(id -> {
                    if (UriUtils.isURI(id)) {
                        return URI.create(id);
                    }
                    return null;
                });
    }

    public static JsonObjectBuilder setId(JsonObjectBuilder objectBuilder, String property, URI id) {
        return setId(objectBuilder, property, id.toString());
    }

    public static JsonObjectBuilder setId(JsonObjectBuilder objectBuilder, String property, String id) {
        objectBuilder.add(property,
                Json.createArrayBuilder()
                        .add(Json.createObjectBuilder().add(Keywords.ID, id)));

        return objectBuilder;
    }

    public static JsonObjectBuilder setValue(JsonObjectBuilder objectBuilder, String property, String type, String value) {

        objectBuilder.add(property, JsonLdValueObject.toJson(type, value));

        return objectBuilder;
    }

    public static JsonObjectBuilder setValue(JsonObjectBuilder objectBuilder, String property, String value) {
        objectBuilder.add(property, JsonLdValueObject.toJson(value));
        return objectBuilder;
    }

    public static JsonObjectBuilder setValue(JsonObjectBuilder objectBuilder, String property, Instant instant) {
        return setValue(objectBuilder, property, XSD_DATE_TIME, instant.toString());
    }

    public static Optional<JsonObject> findFirstObject(JsonValue expanded) {
        if (JsonUtils.isArray(expanded)) {

            for (JsonValue item : expanded.asJsonArray()) {
                if (JsonUtils.isObject(item)) {
                    return Optional.of(item.asJsonObject());
                }
            }

        } else if (JsonUtils.isObject(expanded)) {
            return Optional.of(expanded.asJsonObject());
        }

        return Optional.empty();
    }
}
