package com.apicatalog.jsonld.schema.adapter;

import com.apicatalog.ld.DocumentError;

public interface LdValueAdapter<A, B> {

    B read(A value) throws DocumentError;

    A write(B value) throws DocumentError;

}
