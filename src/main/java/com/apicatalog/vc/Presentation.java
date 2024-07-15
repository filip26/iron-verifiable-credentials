package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

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

    protected Presentation(ModelVersion version) {
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

    public Collection<Credential> credentials() {
        return credentials;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#dfn-holders">Holder</a>
     * @return {@link URI} identifying the holder
     */
    public URI holder() {
        return holder;
    }

    public void credentials(Collection<Credential> credentials) {
        this.credentials = credentials;
    }

    @Override
    public void validate() throws DocumentError {
        if (credentials == null || credentials.isEmpty()) {
            throw new DocumentError(ErrorType.Missing, "VerifiableCredentials");
        }
    }
}
