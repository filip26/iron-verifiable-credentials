package com.apicatalog.vc.model;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

public interface VerifiableAdapterProvider {

    VerifiableAdapter adapter(Collection<String> context) throws DocumentError;
}
