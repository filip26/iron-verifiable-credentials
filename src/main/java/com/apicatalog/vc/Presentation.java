package com.apicatalog.vc;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.vc.holder.PresentationHolder;

/**
 * A generic verifiable presentation.
 */
@Fragment(generic = true)
public interface Presentation extends Verifiable {

    @Term
    PresentationHolder holder();

    @Provided
    @Term("verifiableCredential")
    Collection<Credential> credentials();

    @Override
    default void validate() throws DocumentError {
        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }
        // credentials
        if (credentials() != null && !credentials().isEmpty()) {
            for (Credential credential : credentials()) {
                credential.validate();
            }
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