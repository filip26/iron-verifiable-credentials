package com.apicatalog.vc.holder;

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
