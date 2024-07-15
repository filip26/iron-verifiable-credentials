package com.apicatalog.vc.issuer.reader;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.issuer.IssuerDetails;

public class ExpandedIssuerDetails extends IssuerDetails {

    protected final ModelVersion version;
    
    public ExpandedIssuerDetails(ModelVersion version) {
        this.version = version;
    }
    
    public void id(URI id) {
        this.id = id;
    }
    
    public void type(Collection<String> type) {
        this.type = type;
    }
}
