package com.apicatalog.ld.schema;

import java.util.function.Function;

import jakarta.json.JsonValue;

public class LdProperty<T> {

    protected final LdTerm term;
    protected final LdValueAdapter<JsonValue, T> adapter;
    protected final LdTag tag;
    
    public LdProperty(LdTerm term, LdValueAdapter<JsonValue, T> adapter) {
        this(term, adapter, null);
    }

    public LdProperty(LdTerm term, LdValueAdapter<JsonValue, T> adapter, LdTag tag) {
        this.term = term;
        this.adapter = adapter;
        this.tag = tag;
    }

    public LdProperty<T> required() {
        //TODO
        return this;
    }
    
    public LdProperty<T> optional() {
        return this;
    }
    
    public LdProperty<T> test(Function<T, Boolean> fnc) {
        //TODO
        return this;
    }

    public LdTerm term() {
        return term;
    }

    public JsonValue write(T value) {
        return adapter.write(value);        
    }

    public T read(JsonValue value) {
        return adapter.read(value);
    }

    public LdTag tag() {
        return tag;
    }

}
