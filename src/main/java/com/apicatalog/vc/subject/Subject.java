package com.apicatalog.vc.subject;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.ModelVersion;

public class Subject {
    
    protected final ModelVersion version;
    
    protected URI id;
    protected Collection<String> type;
    
    protected Subject(ModelVersion version) {
        this.version = version;
    }

    public void validate() throws DocumentError {
    }

}
