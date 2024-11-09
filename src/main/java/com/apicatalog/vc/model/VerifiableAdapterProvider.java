package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

public interface VerifiableAdapterProvider {

    VerifiableAdapter adapter(VerifiableModel model) throws DocumentError;
}
