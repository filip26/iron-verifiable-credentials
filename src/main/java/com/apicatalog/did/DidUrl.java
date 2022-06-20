package com.apicatalog.did;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import com.apicatalog.jsonld.StringUtils;

public class DidUrl extends Did {

    private static final long serialVersionUID = -3952644987975446496L;

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

    public static Did from(final URI uri) {

        if (!isDidUrl(uri)) {
            throw new IllegalArgumentException("The URI [" + uri + "] is not valid DID URL, does not start with 'did:'.");
        }

        return from(uri, uri.getSchemeSpecificPart().split(":"), 3);
    }

    
    public static boolean isDidUrl(final URI uri) {
        return Did.SCHEME.equals(uri.getScheme());
    }

    public static boolean isDidUrl(final String uri) {
        if (StringUtils.isBlank(uri)) {
            return false;
        }

        final String[] parts = uri.split(":");

        return (parts.length == 3 || parts.length == 4)
                && Did.SCHEME.equalsIgnoreCase(parts[0])
                ;
    }

    @Override
    public URI toUri() {
        try {
            return new URI(SCHEME, method + ":" + methodSpecificId, path, query, fragment);
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
        final StringBuilder builder = new StringBuilder(super.toString());

        if (StringUtils.isNotBlank(path)) {
            if (path.charAt(0) != '/') {
                builder.append('/');
            }
            builder.append(path);
        }

        if (StringUtils.isNotBlank(query)) {
            if (query.charAt(0) != '?') {
                builder.append('?');
            }
            builder.append(query);
        }

        if (StringUtils.isNotBlank(fragment)) {
            if (fragment.charAt(0) != '#') {
                builder.append('#');
            }
            builder.append(fragment);
        }

        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(fragment, path, query);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DidUrl other = (DidUrl) obj;
        return Objects.equals(fragment, other.fragment) && Objects.equals(path, other.path)
                && Objects.equals(query, other.query);
    }
}
