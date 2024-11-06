package com.apicatalog.vc;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.vc.holder.PresentationHolder;

/**
 * A generic verifiable presentation.
 */
@Fragment(generic = true)
public interface Presentation extends Verifiable {

    @Term
    PresentationHolder holder();

    @Term
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