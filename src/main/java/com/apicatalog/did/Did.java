package com.apicatalog.did;

import java.net.URI;
import java.util.Objects;

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

        return from(uri, uri.getSchemeSpecificPart().split(":"), 3);
    }
    
    public static Did from(final String uri) {

        if (!isDid(uri)) {
            throw new IllegalArgumentException("The URI [" + uri + "] is not valid DID key, does not start with 'did:'.");
        }

        return from(uri, uri.split(":"), 4);
    }

    protected static Did from(final Object uri, final String[] parts, int max) {
        
        if (parts.length < max - 1
                || parts.length > max
                || StringUtils.isBlank(parts[max - 3])
                || StringUtils.isBlank(parts[max - 2])
                ) {
            throw new IllegalArgumentException("The URI [" + uri + "] is not valid DID, must be in form 'did:method:method-specific-id'.");
        }

        String methodSpecificId = parts[max - 2];
        String version = "1";   // default DID version

        if (parts.length == max) {
            if (StringUtils.isBlank(parts[max - 1])) {
                throw new IllegalArgumentException("The URI [" + uri + "] is not valid DID, must be in form 'did:method:method-specific-id'.");
            }
            version = parts[max - 2]; 
            methodSpecificId = parts[max - 1];
        }

        return new Did(parts[max - 3], version, methodSpecificId);
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

    @Override
    public int hashCode() {
        return Objects.hash(method, methodSpecificId, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Did other = (Did) obj;
        return Objects.equals(method, other.method) && Objects.equals(methodSpecificId, other.methodSpecificId)
                && Objects.equals(version, other.version);
    }
}
