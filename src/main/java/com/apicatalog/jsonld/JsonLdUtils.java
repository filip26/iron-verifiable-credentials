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

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JsonLdUtils {

    static final String XSD_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";

    protected JsonLdUtils() {
    }

    /**
     * Checks if the given {@link JsonValue} is {@link JsonObject} has the given
     * type listed as one of its <code>@type</code> declarations.
     *
     * @param type
     * @param value
     * @return <code>true</code> if the given value is {@link JsonObject} and has
     *         property @type including the given type
     */
    public static boolean isTypeOf(final String type, final JsonValue value) {

        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("The 'type' parameter must not be null nor blank.");
        }

        if (value == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        return JsonUtils.isObject(value) && value.asJsonObject().containsKey(Keywords.TYPE)
                && JsonUtils.toStream(value.asJsonObject().get(Keywords.TYPE))
                        .filter(JsonUtils::isString).map(JsonString.class::cast)
                        .map(JsonString::getString).filter(StringUtils::isNotBlank)
                        .anyMatch(type::equals);
    }

    public static Collection<String> getType(final JsonObject value) {

        if (value == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        return JsonUtils.toStream(value.get(Keywords.TYPE)).filter(JsonUtils::isString)
                .map(JsonString.class::cast).map(JsonString::getString)
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
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

        return JsonUtils.toStream(value).filter(JsonUtils::isObject).map(JsonValue::asJsonObject)
                .map(o -> isTypeOf(XSD_DATE_TIME, o)).findAny().orElse(false);
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

        if (JsonUtils.isObject(value) && value.asJsonObject().containsKey(Keywords.GRAPH)
                && value.asJsonObject().size() == 1) {
            value = value.asJsonObject().get(Keywords.GRAPH);
        }

        return JsonUtils.toCollection(value);
    }

    public static Optional<URI> getId(JsonValue subject, String property)
            throws InvalidJsonLdValue {

        if (JsonUtils.isNotObject(subject) || !hasPredicate(subject.asJsonObject(), property)) {
            return Optional.empty();
        }

        final Optional<JsonValue> object = JsonLdUtils.getObjects(subject.asJsonObject(), property)
                .stream().findFirst();

        if (!object.isPresent()) {
            return Optional.empty();
        }

        JsonValue value = object.get();

        if (JsonUtils.isObject(value)) {
            value = value.asJsonObject().get(Keywords.ID);
        }

        if (JsonUtils.isNull(value)) {
            return Optional.empty();
        }

        if (JsonUtils.isString(value)) {
            final String id = ((JsonString) value).getString();

            if (UriUtils.isURI(id)) {
                return Optional.of(URI.create(id));
            }
            throw new InvalidJsonLdValue(property, value,
                    "Property [" + property + "] @id value [" + id + "] is not valid URI.");

        }
        throw new InvalidJsonLdValue(property, value, "Property [" + property + "] @id value ["
                + value + "] is not JSON string but [" + value.getValueType() + "].");
    }

    public static Instant getXsdDateTime(final JsonValue subject, final String property)
            throws InvalidJsonLdValue {

        if (JsonUtils.isNotObject(subject) || !hasPredicate(subject.asJsonObject(), property)) {
            return null;
        }

        final Optional<JsonValue> propertyValue = JsonLdUtils
                .getObjects(subject.asJsonObject(), property).stream().findFirst();

        if (!propertyValue.isPresent()) {
            return null;
        }

        JsonValue value = propertyValue.get();

        if (isXsdDateTime(value)) {
            return JsonUtils.toStream(value).filter(ValueObject::isValueObject)
                    .filter(item -> isTypeOf(XSD_DATE_TIME, item.asJsonObject()))
                    .map(ValueObject::getValue).filter(Optional::isPresent).map(Optional::get)
                    .filter(JsonUtils::isString).findFirst().map(JsonString.class::cast)
                    .map(JsonString::getString).map(datetimeValue -> {
                        try {
                            return Instant.parse(datetimeValue);

                        } catch (DateTimeParseException e) {
                            // invalid date time format
                        }
                        return null;
                    }).orElseThrow(() -> new InvalidJsonLdValue(property, value, "The property ["
                            + property + "] value is not valid XsdDateTime but [" + value + "]."));
        }
        throw new InvalidJsonLdValue(property, value,
                "The property [" + property + "] @type is not XsdDateTime.");
    }

    public static final Optional<URI> getId(JsonValue subject) throws InvalidJsonLdValue {

        if (subject == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }

        final Optional<JsonValue> propertyValue = JsonLdUtils.findFirstObject(subject)
                .map(o -> o.get(Keywords.ID));

        if (!propertyValue.isPresent()) {
            return Optional.empty();
        }

        final JsonValue value = propertyValue.get();

        if (JsonUtils.isNull(value)) {
            return Optional.empty();
        }

        if (JsonUtils.isString(value)) {
            final String id = ((JsonString) value).getString();

            if (UriUtils.isURI(id)) {
                return Optional.of(URI.create(id));
            }
            throw new InvalidJsonLdValue(Keywords.ID, value,
                    "Property [" + Keywords.ID + "] value [" + id + "] is not valid URI.");

        }
        throw new InvalidJsonLdValue(Keywords.ID, value, "Property [" + Keywords.ID + "] value ["
                + value + "] is not JSON string but [" + value.getValueType() + "].");
    }

    public static JsonObjectBuilder setId(JsonObjectBuilder objectBuilder, String property,
            URI id) {
        return setId(objectBuilder, property, id.toString());
    }

    public static JsonObjectBuilder setId(JsonObjectBuilder objectBuilder, String property,
            String id) {
        objectBuilder.add(property,
                Json.createArrayBuilder().add(Json.createObjectBuilder().add(Keywords.ID, id)));

        return objectBuilder;
    }

    public static JsonObjectBuilder setValue(JsonObjectBuilder objectBuilder, String property,
            String type, String value) {

        objectBuilder.add(property, JsonLdValueObject.toJson(type, value));

        return objectBuilder;
    }

    public static JsonObjectBuilder setValue(JsonObjectBuilder objectBuilder, String property,
            String value) {
        objectBuilder.add(property, JsonLdValueObject.toJson(value));
        return objectBuilder;
    }

    public static JsonObjectBuilder setValue(JsonObjectBuilder objectBuilder, String property,
            Instant instant) {
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
