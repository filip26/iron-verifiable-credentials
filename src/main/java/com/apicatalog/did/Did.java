package com.apicatalog.did;

import java.net.URI;

import com.apicatalog.jsonld.StringUtils;

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
        return Did.SCHEME.equals(uri.getScheme());      //FIXME path .. #fragment must be blank
    }

    public static boolean isDid(final String uri) {
        return uri != null && uri.toLowerCase().startsWith(SCHEME + ":");      //FIXME path .. #fragment must be blank
    }
    
    /**
     * Creates a new DID instance from the given {@link URI}.
     *
     * @param uri The source URI to be transformed into DID
     * @return The new DID
     * 
     * @throws NullPointerException
     *         If {@code uri} is {@code null}
     *         
     * @throws IllegalArgumentException
     *         If the given {@code uri} is not valid DID
     */    
    public static Did from(final URI uri) {

        if (!isDid(uri)) {
            throw new IllegalArgumentException("The URI [" + uri + "] is not valid DID key, does not start with 'did:'.");
        }

        final String[] parts = uri.getSchemeSpecificPart().split(":");
        
        if (parts.length < 2 || parts.length > 3 || StringUtils.isBlank(parts[0]) || StringUtils.isBlank(parts[1])) {
            throw new IllegalArgumentException("The URI [" + uri + "] is not valid DID, must be in form 'did:method:method-specific-id'.");
        }
               
        String methodSpecificId = parts[1];
        String version = "1";   // default DID version

        if (parts.length == 3) {
            if (StringUtils.isBlank(parts[2])) {
                throw new IllegalArgumentException("The URI [" + uri + "] is not valid DID, must be in form 'did:method:method-specific-id'.");
            }
            version = parts[1]; 
            methodSpecificId = parts[2];
        }

        return new Did(parts[0], version, methodSpecificId);
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
    
    public boolean isDidUrl() {
        return false;
    }
    
    public DidUrl asDidUrl() {
        throw new ClassCastException();
    }
    
    @Override
    public String toString() {
        return Did.SCHEME + ":" + method + (!"1".equals(version) ? ":" + version : "") + ":" + methodSpecificId; 
    }    
    
    //TODO add equals and hashCode
}
