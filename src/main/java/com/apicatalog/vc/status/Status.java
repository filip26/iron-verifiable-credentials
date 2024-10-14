package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.orm.Fragment;

@Fragment(generic = true)
public interface Status {

    URI id();

    Collection<String> type();

    /**
     * A custom validation implemented by an ancestor.
     * 
     * @throws DocumentError
     */
    default void validate() throws DocumentError {
        // custom validation
    }

}
