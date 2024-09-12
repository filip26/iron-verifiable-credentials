package com.apicatalog.vc.jsonld;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.reader.VerifiableReader;

public interface JsonLdVerifiableAdapter {

    VerifiableReader reader(Collection<String> contexts) throws DocumentError;
    
}
