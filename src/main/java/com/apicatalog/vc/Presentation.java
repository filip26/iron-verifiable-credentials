package com.apicatalog.vc;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.holder.PresentationHolder;

public interface Presentation extends Verifiable {

    PresentationHolder holder();
    
    Collection<Credential> credentials();

    @Override
    default void validate() throws DocumentError {
        if (credentials() == null || credentials().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, "VerifiableCredentials");
        }
    }

    @Override
    default boolean isPresentation() {
        return true;
    }

    @Override
    default Presentation asPresentation() {
        return this;
    }
}
