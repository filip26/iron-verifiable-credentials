package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

/**
 * Represents a verifiable presentations (VP).
 *
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model/#presentations">Presentations</a>
 *      
 * @since 0.9.0
 */
public class Presentation extends Verifiable {

    protected URI holder;

    protected Collection<Credential> credentials;

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