package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.vc.model.DocumentError;

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
