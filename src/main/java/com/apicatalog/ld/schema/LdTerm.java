package com.apicatalog.ld.schema;

import com.apicatalog.jsonld.lang.Keywords;

public class LdTerm {

    protected static final LdTerm ID = new LdTerm(Keywords.ID);
    protected static final LdTerm TYPE = new LdTerm(Keywords.TYPE);
    
    final String name;
    final String vocabulary;
    final String id;
    
    LdTerm(String name) {
        this.name = name;
        this.vocabulary = null;
        this.id = name;
    }

    LdTerm(String name, String vocabulary) {
        this.name = name;
        this.vocabulary = vocabulary;
        this.id = vocabulary + name;
    }
    
    public static final LdTerm create(String name, String vocabulary) {
        return new LdTerm(name, vocabulary);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
}
