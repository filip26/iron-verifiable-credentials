package com.apicatalog.ld.schema;

import java.util.Map;


@FunctionalInterface
public interface ParametrizedPredicate<T> {

    boolean test(T value, Map<String, Object> params);

}
