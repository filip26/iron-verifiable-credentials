package com.apicatalog.ld.schema;

import jakarta.json.JsonObject;

public class LdObject extends LdSchema implements LdValue<JsonObject, JsonObject> {

    @Override
    public JsonObject apply(JsonObject value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject inverse(JsonObject value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> LdValue<JsonObject, X> map(LdValueAdapter<JsonObject, X> adapter) {
        // TODO Auto-generated method stub
        return null;
    }

    public static LdObject create(LdProperty<?>[] properties) {
        // TODO Auto-generated method stub
        return null;
    }

    
     
}
