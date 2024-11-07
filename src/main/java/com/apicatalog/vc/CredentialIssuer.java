package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;

@Fragment(generic = true)
public interface CredentialIssuer {

    @Id
    URI id();

    /**
     * A custom validation implemented by an ancestor.
     * 
     * @throws DocumentError
     */
    default void validate() throws DocumentError {
        if (id() == null) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.ID);
        }
    }
}
