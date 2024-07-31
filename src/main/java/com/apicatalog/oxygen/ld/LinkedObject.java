package com.apicatalog.oxygen.ld;

import java.net.URI;
import java.util.Collection;

public interface LinkedObject extends LinkedData {

    URI id();

    Collection<String> type();

    @Override
    default boolean isObject() {
        return true;
    }

    @Override
    default LinkedObject asObject() {
        return this;
    }
}
