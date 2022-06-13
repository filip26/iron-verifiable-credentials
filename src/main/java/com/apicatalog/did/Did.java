package com.apicatalog.did;

import java.net.URI;

public class Did  {

    public static final String SCHEME = "did";

    protected final String method;
    protected final String version;
    protected final String methodSpecificId;
    
    protected Did(String method, String version, String methodSpecificId) {
        this.method = method;
        this.version = version;
        this.methodSpecificId = methodSpecificId;
    }
    
    public static boolean isDid(final URI uri) {
        return Did.SCHEME.equals(uri.getScheme());
    }

    public static boolean isDid(final String uri) {
        return uri != null && uri.toLowerCase().startsWith(SCHEME + ":");
    }

    public String getMethod() {
        return method;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getMethodSpecificId() {
        return methodSpecificId;
    }
    
    public URI toUri() {
        return URI.create(toString());
    }
    
    @Override
    public String toString() {
        return Did.SCHEME + ":" + method + (!"1".equals(version) ? ":" + version : "") + ":" + methodSpecificId; 
    }
}
