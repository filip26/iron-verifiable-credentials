package com.apicatalog.jsonld.schema;

import java.io.Serializable;
import java.util.Objects;

import com.apicatalog.jsonld.lang.Keywords;

public final class LdTerm implements Serializable {

    private static final long serialVersionUID = -6633084683490148231L;

    public static final LdTerm ID = new LdTerm(Keywords.ID);
    public static final LdTerm TYPE = new LdTerm(Keywords.TYPE);

    final String name;
    final String vocabulary;
    final String uri;

    LdTerm(String name) {
        this.name = name;
        this.vocabulary = null;
        this.uri = name;
    }

    LdTerm(String name, String vocabulary) {
        this.name = name;
        this.vocabulary = vocabulary;
        this.uri = vocabulary + name;
    }

    public static final LdTerm create(String name, String vocabulary) {
        return new LdTerm(name, vocabulary);
    }

    public String uri() {
        return uri;
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
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
        return Objects.equals(uri, other.uri);
    }

    @Override
    public String toString() {
        return uri;
    }
}
