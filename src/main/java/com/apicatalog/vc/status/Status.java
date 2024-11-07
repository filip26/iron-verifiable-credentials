package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.linkedtree.orm.Type;

@Fragment(generic = true)
public interface Status {

    @Id
    URI id();

    @Type
    Collection<String> type();

    /**
     * A custom validation implemented by an ancestor.
     * 
     * @throws DocumentError
     */
    default void validate() throws DocumentError {
        // custom
    }
}
