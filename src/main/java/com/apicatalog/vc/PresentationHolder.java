package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;

@Fragment(generic = true)
public interface PresentationHolder {

    @Id
    URI id();

    /**
     * Custom validation
     * @throws DocumentError
     */
    default void validate() throws DocumentError {
        // custom validation
    }

}
