package com.apicatalog.ld.node.adapter;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.oxygen.ld.LinkedData;

public interface LdAdapter<T> {

    T read(LinkedData value) throws DocumentError;
    
    LinkedData write(T value);
}
