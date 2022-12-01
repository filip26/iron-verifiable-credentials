package com.apicatalog.ld.schema.adapter;

import java.net.URI;

import com.apicatalog.jsonld.uri.UriUtils;
import com.apicatalog.ld.schema.LdValueAdapter;

public class UriAdapter implements LdValueAdapter<String, URI> {

    @Override
    public URI read(String value) {

        return UriUtils.create(value);
    }

    @Override
    public String write(URI value) {
        return value.toString();
    }

}
