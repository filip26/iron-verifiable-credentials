package com.apicatalog.did;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.apicatalog.jsonld.StringUtils;

public class Did implements Serializable {

    private static final long serialVersionUID = -2179410886513458650L;

    public static final String SCHEME = "did";

    protected final String method;
    protected final String version;
    protected final String methodSpecificId;

    protected Did(final String method, final String version, final String methodSpecificId) {
        this.method = method;
        this.version = version;
        this.methodSpecificId = methodSpecificId;
    }

    public static boolean isDid(final URI uri) {
        if (!Did.SCHEME.equalsIgnoreCase(uri.getScheme())
                || StringUtils.isBlank(uri.getSchemeSpecificPart())
                || StringUtils.isNotBlank(uri.getAuthority())
                || StringUtils.isNotBlank(uri.getUserInfo())
                || StringUtils.isNotBlank(uri.getHost())
                || StringUtils.isNotBlank(uri.getPath())
                || StringUtils.isNotBlank(uri.getQuery())
                || StringUtils.isNotBlank(uri.getFragment())
                ) {
                  return false;
                }

        final String[] parts = uri.getSchemeSpecificPart().split(":");

        return parts.length == 2 || parts.length == 3;
    }

    public static boolean isDid(final String uri) {

        if (StringUtils.isBlank(uri)) {
            return false;
        }

        final String[] parts = uri.split(":");

        return (parts.length == 3 || parts.length == 4)
                && Did.SCHEME.equalsIgnoreCase(parts[0])
                && !parts[parts.length - 1].contains("/")       // path
                && !parts[parts.length - 1].contains("?")       // query
                && !parts[parts.length - 1].contains("#")       // fragment
                ;
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

    /**
     * Creates a new DID instance from the given URI.
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
        try {
            return new URI(SCHEME, method + ":" + methodSpecificId, null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean isDidUrl() {
        return false;
    }

    public DidUrl asDidUrl() {
        throw new ClassCastException();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder()
                    .append(SCHEME)
                    .append(':')
                    .append(method)
                    .append(':');

        if (!"1".equals(version)) {
            builder
                .append(version)
                .append(':');
        }
        return builder.append(methodSpecificId).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, methodSpecificId, version);
    }

    @Override
    public boolean equals(final Object obj) {
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
