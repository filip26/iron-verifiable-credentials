package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
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
        // custom validation
    }
}
