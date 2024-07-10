package com.apicatalog.ld;

import jakarta.json.JsonValue;

public interface Expandable<T extends JsonValue> {

    // id?
    // type?
    
    T expand();

}
