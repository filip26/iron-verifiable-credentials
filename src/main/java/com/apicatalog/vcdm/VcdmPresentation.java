package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;

/**
 * A common ancestor to W3C VCDM based verifiable presentations.
 */
@Fragment
@Term("VerifiablePresentation")
public interface VcdmPresentation extends VcdmVerifiable, Presentation {

    @Provided
    @Term("verifiableCredential")
    @Override
    Collection<Credential> credentials();

    @Override
    default void validate() throws DocumentError {
        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }
        // credentials
        if (credentials() == null || credentials().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.VERIFIABLE_CREDENTIALS);
        }
    }
}
