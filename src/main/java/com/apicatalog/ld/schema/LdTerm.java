package com.apicatalog.ld.schema;

import java.util.Objects;

import com.apicatalog.jsonld.lang.Keywords;

public class LdTerm {

    public static final LdTerm ID = new LdTerm(Keywords.ID);
    public static final LdTerm TYPE = new LdTerm(Keywords.TYPE);
    
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LdTerm other = (LdTerm) obj;
        return Objects.equals(id, other.id);
    }
}
