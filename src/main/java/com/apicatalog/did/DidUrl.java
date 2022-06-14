package com.apicatalog.did;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

    public static DidUrl from(URI uri) {
        //TODO return new DidUrl(Did.from(uri), path, query, fragment);
        return null;
    }
    
    public static boolean isDidUrl(final URI uri) {
        return Did.SCHEME.equals(uri.getScheme());
    }

    public static boolean isDidUrl(final String uri) {
        return uri != null && uri.toLowerCase().startsWith(SCHEME + ":");
    }
    
    @Override
    public URI toUri() {
        try {
            return new URI(SCHEME, method + ":" + methodSpecificId, null);      //FIXME
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public URL toUrl() {
        try {
            return toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public boolean isDidUrl() {
        return true;
    }

    @Override
    public DidUrl asDidUrl() {
        return this;
    }

    @Override
    public String toString() {        
        return toUri().toString();
    }

    //TODO add equals and hashCode
}
