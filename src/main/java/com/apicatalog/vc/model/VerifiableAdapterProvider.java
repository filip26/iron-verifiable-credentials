package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

@FunctionalInterface
public interface VerifiableAdapterProvider {

    VerifiableAdapter adapter(VerifiableModel model) throws DocumentError;
}
