package com.apicatalog.ld.schema.value;

import com.apicatalog.ld.schema.LdValue;
import com.apicatalog.ld.schema.LdValueAdapter;

public class MultibaseValue implements LdValue<String, byte[]> {

    @Override
    public byte[] apply(String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String inverse(byte[] value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> LdValue<String, X> map(LdValueAdapter<byte[], X> adapter) {
        // TODO Auto-generated method stub
        return null;
    }

}
