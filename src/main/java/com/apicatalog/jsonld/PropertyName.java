package com.apicatalog.jsonld;

public class PropertyName {

    final String name;
    final String vocabulary;
    final String id;

    PropertyName(String name, String vocabulary) {
        this.name = name;
        this.vocabulary = vocabulary;
        this.id = vocabulary + name;
    }
    
    public static final PropertyName create(String name, String vocabulary) {
        return new PropertyName(name, vocabulary);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
}
