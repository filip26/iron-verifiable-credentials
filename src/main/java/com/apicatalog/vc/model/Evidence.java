package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;

public interface Evidence extends Linkable {

    URI id();

    Collection<String> type();
    
    void validate() throws DocumentError;
    
}
