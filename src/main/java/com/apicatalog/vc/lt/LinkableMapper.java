package com.apicatalog.vc.lt;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;

@Deprecated
@FunctionalInterface
public interface LinkableMapper<R> {

    public R map(Linkable linkable) throws DocumentError;
    
}
