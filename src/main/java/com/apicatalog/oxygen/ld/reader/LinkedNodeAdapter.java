package com.apicatalog.oxygen.ld.reader;

import com.apicatalog.oxygen.ld.LinkedNode;

public interface LinkedNodeAdapter<T extends LinkedNode, I> {

    T materialize(LinkedAdapterContext ctx, I value);
    
}
