package com.apicatalog.ld.schema;

public interface LdValueAdapter<A, B> {

    B apply(A value);
    A inverse(B value);
}
