package com.apicatalog.oxygen.ld;

import java.net.URI;
import java.util.Collection;

public interface LinkedNode extends LinkedData {

    URI id();

    Collection<String> type();

    @Override
    default boolean isObject() {
        return true;
    }

    @Override
    default LinkedNode asObject() {
        return this;
    }
    
    Collection<String> terms();

    Collection<LinkedData> values(String term);    
}
