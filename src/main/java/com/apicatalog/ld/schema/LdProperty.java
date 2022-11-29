package com.apicatalog.ld.schema;

import java.util.function.Function;

import jakarta.json.JsonValue;

public class LdProperty<T> {

    public enum Tag {
        ProofValue,
    }
    
    protected final LdTerm id;
    protected final LdValue<JsonValue, T> value;
    protected final Tag tag;
    
    public LdProperty(LdTerm id, LdValue<JsonValue, T> value) {
        this(id, value, null);
    }

    public LdProperty(LdTerm id, LdValue<JsonValue, T> value, Tag tag) {
        this.id = id;
        this.value = value;
        this.tag = tag;
    }

    public LdProperty<T> required() {
        return null;
    }
    
    public LdProperty<T> optional() {
        return this;
    }
    
    public LdProperty<T> test(Function<T, Boolean> fnc) {
        return null;
    }

    public LdProperty<T> defaultValue(T value) {
        return null;
    }

}
