package com.apicatalog.ld;

import java.net.URI;
import java.util.List;
import java.util.Set;

public interface LdNode extends LdValue {

    URI getId();
    
    Set<URI> getTypes();

    List<LdValue> getValues(URI predicate);
    
    Set<URI> getPredicates();

}
