package com.apicatalog.did;

import java.net.URL;

public class DidUrl extends Did {

    protected final String path;
    protected final String query;
    protected final String fragment;
    
    protected DidUrl(Did did, String path, String query, String fragment) {
        super(did.method, did.version, did.methodSpecificId);
        this.path = path;
        this.query = query;
        this.fragment = fragment;
    }
    
    public static DidUrl from(Did did, String path, String query, String fragment) {
        return new DidUrl(did, path, query, fragment);
    }
    
    public URL toUrl() {
        //TODO
        throw new UnsupportedOperationException();
    }
}
