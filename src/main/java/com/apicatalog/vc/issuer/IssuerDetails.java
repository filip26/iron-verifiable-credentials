package com.apicatalog.vc.issuer;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;

public class IssuerDetails {

    protected URI id;
    protected Collection<String> type;

    protected IssuerDetails() {
        /* protected */
    }

    public URI id() {
        return id;
    }

    public void validate() throws DocumentError {
        // issuer - @id
        if (id == null) {
            throw new DocumentError(ErrorType.Invalid, VcVocab.ISSUER);
        }
    }

}
