package com.apicatalog.ld.schema.value;

import java.net.URI;
import java.util.function.Function;

import com.apicatalog.ld.schema.LdValue;
import com.apicatalog.ld.schema.LdValueAdapter;

import jakarta.json.JsonValue;

public class UriValue implements LdValue<String, URI> {

    @Override
    public URI apply(String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String inverse(URI value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> LdValue<String, X> map(LdValueAdapter<URI, X> adapter) {
        // TODO Auto-generated method stub
        return null;
    }


}
