package com.apicatalog.vc.reader;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

public interface ReaderResolver {

    VerifiableReader resolveReader(Collection<String> contexts) throws DocumentError;
    
}
