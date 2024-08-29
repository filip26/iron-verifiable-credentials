package com.apicatalog.vc.jsonld;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

public interface JsonLdVerifiableAdapter {

    JsonLdVerifiableReader reader(Collection<String> contexts) throws DocumentError;
    
}
