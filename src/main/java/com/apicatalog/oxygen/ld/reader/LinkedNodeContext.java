package com.apicatalog.oxygen.ld.reader;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.oxygen.ld.LinkedData;

public interface LinkedNodeContext {

    LinkedNodeContext parent();
    
    URI id();
    Collection<String> type();
    
    String term();
    int index();
}
