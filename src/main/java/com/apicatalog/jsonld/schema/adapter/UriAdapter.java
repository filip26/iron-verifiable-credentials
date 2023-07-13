package com.apicatalog.jsonld.schema.adapter;

import java.net.URI;

import com.apicatalog.jsonld.uri.UriUtils;

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
