package com.apicatalog.ld.schema;

public interface LdValueAdapter<A, B> extends LdValue<B> {

    B read(A value);
    A write(B value);
    
//    <X> LdValueAdapter<A ,X> map(LdValueAdapter<B, X> adapter);

}
