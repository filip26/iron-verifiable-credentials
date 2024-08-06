package com.apicatalog.oxygen.ld.reader;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.oxygen.ld.LinkedData;

public interface LinkedAdapterContext {

    LinkedAdapterContext parent();
    
    URI id();
    Collection<String> type();
    
    String term();
    int index();
    
    //Collection<LinkedData> materialize(id, types, term)
}
