package com.apicatalog.vc.status.reader;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.vc.status.Status;

public class ExpandedStatus extends Status {

    public void id(URI id) {
        this.id = id;
    }
    
    public void type(Collection<String> type) {
        this.type = type;
    }
}
