package com.apicatalog.ld;

import java.util.Set;

import jakarta.json.JsonValue;

public interface LdMapping {

    /**
     * Supported <code>@type</code> values.
     * @return
     */
    Set<String> getTypes();

    LdValue deserialize(JsonValue value);
    
    JsonValue serialize(LdValue value);
    
}
