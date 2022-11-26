package com.apicatalog.jsonld;

public class PropertyName {

    final String name;
    final String vocabulary;

    PropertyName(String name, String vocabulary) {
        this.name = name;
        this.vocabulary = vocabulary;
    }

    public String id() {
        return vocabulary + name;
    }
}
