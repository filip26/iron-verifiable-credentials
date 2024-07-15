package com.apicatalog.vc.issuer;

import java.net.URI;
import java.util.Collection;

public class IssuerDetails {

    protected URI id;
    protected Collection<String> type;
    
    protected IssuerDetails() {
        /* protected */
    }

    public URI id() {
        return id;
    }

}
