package com.apicatalog.vc.reader;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

public interface VerifiableReaderResolver {

    VerifiableReader resolveReader(Collection<String> contexts) throws DocumentError;
    
}
