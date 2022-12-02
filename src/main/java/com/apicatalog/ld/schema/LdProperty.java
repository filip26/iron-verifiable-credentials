package com.apicatalog.ld.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonValue;

public class LdProperty<T> {

    protected final LdTerm term;
    protected final LdValueAdapter<JsonValue, T> adapter;
    protected final String tag;
    protected boolean mandatory;
    
    protected Collection<ParametrizedPredicate<T>> predicates;
    
    public LdProperty(LdTerm term, LdValueAdapter<JsonValue, T> adapter) {
        this(term, adapter, null);
    }

    public LdProperty(LdTerm term, LdValueAdapter<JsonValue, T> adapter, String tag) {
        this.term = term;
        this.adapter = adapter;
        this.tag = tag;
        this.mandatory = false;
    }

    public LdProperty<T> required() {
        this.mandatory = true;
        return this;
    }
    
    public LdProperty<T> optional() {
        this.mandatory = false;
        return this;
    }
    
    public LdProperty<T> test(Predicate<T> fnc) {
        return test((v, p) -> fnc.test(v));
    }

    public LdProperty<T> test(ParametrizedPredicate<T> fnc) {
        if (predicates == null) {
            predicates = new ArrayList<>(5);
        }
        predicates.add(fnc);
        return this;
    }

    public LdTerm term() {
        return term;
    }

    public JsonValue write(T value) throws DocumentError {
        return adapter.write(value);        
    }

    public T read(JsonValue value) throws DocumentError {
        return adapter.read(value);
    }

    public String tag() {
        return tag;
    }

    public boolean validate(T value, Map<String, Object> params) throws DocumentError {

        if (value == null) {
            if (mandatory) {
                throw new DocumentError(ErrorType.Missing, term);
            }
            return true;
        }
        
        if (predicates == null) {
            return true;
        }
        
        for (final ParametrizedPredicate<T> predicate : predicates) {
            if (!predicate.test(value, params)) {
                throw new DocumentError(ErrorType.Invalid, term);
            }
        }

        return true;
    }

}
