package com.apicatalog.vc.subject;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;

public interface Subject extends Linkable {

    URI id();
    
    default void validate() throws DocumentError {
        throw new UnsupportedOperationException();
    }

}
