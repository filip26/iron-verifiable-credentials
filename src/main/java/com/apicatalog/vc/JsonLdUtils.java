package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import com.apicatalog.jsonld.StringUtils;
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
        return JsonUtils.isObject(value) && value.asJsonObject().containsKey(Keywords.TYPE);
    }

    public static Optional<Instant> getXsdDateTime(JsonValue value)  {

        if (value == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }
        
        for (final JsonValue item : JsonUtils.toJsonArray(value)) {
            
            if (!ValueObject.isValueObject(item) || !isTypeOf(XSD_DATE_TIME, item.asJsonObject())) {
                continue;
            }
            
            
            final Optional<JsonValue> datetimeValue = ValueObject.getValue(item);
            
            if (datetimeValue.isPresent()) {

                if (JsonUtils.isNotString(datetimeValue.get()) /*TODO validate format ...Z */) {
                    //TODO exception
                }
                
                try {
                    final Instant datetitme = Instant.parse(((JsonString)datetimeValue.get()).getString());
    
                    // consider only the first item
                    return Optional.of(datetitme);
                    
                } catch (DateTimeParseException e) {
                    // invalid date time format
                }
            }
    
        }
                
        return Optional.empty();
    }
    
    public static final Optional<URI> getId(JsonValue value) {

        if (JsonUtils.isArray(value)) {
            // consider only the first item
            value = value.asJsonArray().get(0); 
        }

        if (JsonUtils.isObject(value)) {
            value = value.asJsonObject().get(Keywords.ID);
        }
        
        if (JsonUtils.isString(value)) {
            
            final String id = ((JsonString)value).getString();
            
            if (UriUtils.isURI(id)) {
                return Optional.of(URI.create(id));
            }

        }
        
        return Optional.empty();
    }

    public static boolean isXsdDateTime(JsonValue value) {
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

    public static Optional<JsonValue> getProperty(JsonObject object, String base, String property) {
        
        JsonValue value = object.get(base + property);
        
        if (value == null) {
            value = object.get(property);
        }

        return Optional.ofNullable(value); 
    }
}
