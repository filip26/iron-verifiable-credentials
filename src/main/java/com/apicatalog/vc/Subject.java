package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.adapter.HasTerms;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.linkedtree.orm.Injected;
import com.apicatalog.linkedtree.orm.Type;

@Fragment(generic = true)
public interface Subject {

    @Id
    URI id();
    
    @Type
    Collection<String> type();
    
    @Injected(HasTerms.class)
    boolean hasClaims();
    
    /**
     * A custom validation implemented by an ancestor.
     * 
     * @throws DocumentError
     */
    default void validate() throws DocumentError {
        // custom validation
    }
}
