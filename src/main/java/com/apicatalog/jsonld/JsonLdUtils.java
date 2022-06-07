package com.apicatalog.jsonld;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.jsonld.uri.UriUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JsonLdUtils {

    static final String XSD_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";

    protected JsonLdUtils() {}

    /**
     * Checks if the given {@link JsonObject} has the given type listed as one of its @type declarations.
     *
     * @param type
     * @param object
     * @return
     */
    public static boolean isTypeOf(final String type, final JsonObject object) {

        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("The 'type' parameter must not be null nor blank.");
        }

        if (object == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        return object.containsKey(Keywords.TYPE)
                && JsonUtils
                    .toStream(object.get(Keywords.TYPE))
                    .filter(JsonUtils::isString)
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .filter(StringUtils::isNotBlank)
                    .anyMatch(type::equals);
    }

    public static boolean hasType(final JsonValue value) {

        if (value == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }

        return JsonUtils.isObject(value) && value.asJsonObject().containsKey(Keywords.TYPE);
    }

    public static Optional<Instant> findFirstXsdDateTime(JsonValue value)  {

        if (value == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }

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
                });
    }

    public static final Optional<URI> getId(JsonValue expanded) {
        return findFirstObject(expanded)
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

    public static boolean hasProperty(JsonObject object, String property) {
        return object.containsKey(property);
    }

    public static boolean hasProperty(JsonObject object, String base, String property) {
        return object.containsKey(base + property) || object.containsKey(property);
    }

    public static Optional<JsonValue> getProperty(JsonObject object,String property) {
        return Optional.ofNullable(object.get(property));
    }

    public static Optional<JsonValue> getProperty(JsonObject object, String base, String property) {
        JsonValue value = object.get(base + property);

        if (value == null) {
            value = object.get(property);
        }

        return Optional.ofNullable(value);
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
