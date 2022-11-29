package com.apicatalog.ld.schema;

public interface LdValue<T, R> extends LdValueAdapter<T, R> {

    <X> LdValue<T, X> map(LdValueAdapter<R, X> adapter);

}
