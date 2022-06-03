package com.apicatalog.jsonld;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JsonLdUtils {

    static final String XML_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";
    
    protected JsonLdUtils() {}

    /**
     * Checks if the given {@link JsonObject} has the given type listed as one of its @type declarations.
     *
     * @param type
     * @param object
     * @return
     */
    public static boolean isTypeOf(String type, JsonObject object) {

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

    public static boolean hasTypeDeclaration(JsonObject object) {
        if (object == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }
        return object.containsKey(Keywords.TYPE);
    }
    
    public static Optional<Instant> getDateTime(JsonValue value) throws DateTimeParseException {

        if (JsonUtils.isArray(value)) {
            // consider only the first item
            value = value.asJsonArray().get(0); 
        }
        
        if (!ValueObject.isValueObject(value)) {
            return Optional.empty();
        }
        
        if (isTypeOf(XML_DATE_TIME, value.asJsonObject())) {
            
            final Optional<JsonValue> datetimeValue = ValueObject.getValue(value);
            
            if (datetimeValue.isPresent()) {

                if (JsonUtils.isNotString(datetimeValue.get()) /*TODO validate format ...Z */) {
                    //TODO exception
                }
                
                final Instant datetitme = Instant.parse(((JsonString)datetimeValue.get()).getString());
                
                return Optional.of(datetitme);
            }
        }
        
        return Optional.empty();
    }

}
