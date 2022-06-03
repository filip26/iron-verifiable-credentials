package com.apicatalog.jsonld;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;

public class JsonLdUtils {

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
    
}
