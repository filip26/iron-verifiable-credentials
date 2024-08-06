package com.apicatalog.oxygen.ld.reader;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.oxygen.ld.LinkedData;

public interface LinkedDataReader<O extends LinkedData, I> {
    
    Collection<O> read(I input) throws DocumentError;
    
}
