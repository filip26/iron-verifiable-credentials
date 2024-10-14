package com.apicatalog.vc.subject;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.orm.Fragment;

@Fragment(generic = true)
public interface Subject {

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
