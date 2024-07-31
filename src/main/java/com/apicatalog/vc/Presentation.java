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
public interface Presentation extends Verifiable {

    @Override
    default boolean isPresentation() {
        return true;
    }

    @Override
    default Presentation asPresentation() {
        return this;
    }

    Collection<Credential> credentials();

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#dfn-holders">Holder</a>
     * @return {@link URI} identifying the holder
     */
    URI holder();

    @Override
    default void validate() throws DocumentError {
        if (credentials() == null || credentials().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, "VerifiableCredentials");
        }
    }
}
