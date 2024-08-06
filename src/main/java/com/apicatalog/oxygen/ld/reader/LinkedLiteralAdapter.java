package com.apicatalog.oxygen.ld.reader;

import com.apicatalog.oxygen.ld.LinkedLiteral;

public interface LinkedLiteralAdapter<T extends LinkedLiteral, I> {

    T materialize(LinkedNodeContext ctx, I value);
    
}
