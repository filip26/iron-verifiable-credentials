package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

/**
 * Represents a verifiable presentation (VP).
 *
 * @see <a href= "https://www.w3.org/TR/vc-data-model/#presentations">v1.1</a>
 * @see <a href= "https://w3c.github.io/vc-data-model/#presentations">v2.0</a>
 * 
 * @since 0.9.0
 */
public class Presentation extends Verifiable {

    protected URI holder;

    protected Collection<Credential> credentials;

    public Presentation(DataModelVersion version) {
        super(version);
    }

    @Override
    public boolean isPresentation() {
        return true;
    }

    @Override
    public Presentation asPresentation() {
        return this;
    }

    public Collection<Credential> getCredentials() {
        return credentials;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#dfn-holders">Holder</a>
     * @return {@link URI} identifying the holder
     */
    public URI getHolder() {
        return holder;
    }

    public void setHolder(URI holder) {
        this.holder = holder;
    }

    public void setCredentials(Collection<Credential> credentials) {
        this.credentials = credentials;
    }
}
