package com.apicatalog.vc.processor;

import java.util.Objects;

public class Parameter<V> {

    protected final String name;
    protected final V value;

    protected Parameter(String name, V value) {
        this.name = name;
        this.value = value;
    }

    public static <V> Parameter<V> of(String name, V value) {

        Objects.requireNonNull(name);

        return new Parameter<V>(name, value);
    }

    public String name() {
        return name;
    }

    public V value() {
        return value;
    }
}
