package com.apicatalog.ld.schema;

import com.apicatalog.ld.DocumentError;

public interface LdValueAdapter<A, B> extends LdValue<B> {

    B read(A value) throws DocumentError;

    A write(B value) throws DocumentError;

//    <X> LdValueAdapter<A ,X> map(LdValueAdapter<B, X> adapter);

}
