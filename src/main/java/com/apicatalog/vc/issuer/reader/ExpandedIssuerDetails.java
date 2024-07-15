package com.apicatalog.vc.issuer.reader;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.vc.issuer.IssuerDetails;

public class ExpandedIssuerDetails extends IssuerDetails {

    public void id(URI id) {
        this.id = id;
    }
    
    public void type(Collection<String> type) {
        this.type = type;
    }
}
