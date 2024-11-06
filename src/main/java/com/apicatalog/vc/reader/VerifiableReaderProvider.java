package com.apicatalog.vc.reader;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

public interface VerifiableReaderProvider {

    VerifiableReader reader(Collection<String> contexts) throws DocumentError;

}
