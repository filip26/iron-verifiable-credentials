package com.apicatalog.vc.reader;

import java.net.URI;

import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.Presentation;

import jakarta.json.JsonObject;

public class ExpandedPresentation extends Presentation {

    protected JsonObject expanded;
    
    protected ExpandedPresentation(ModelVersion version, JsonObject expanded) {
        super(version);
        this.expanded = expanded;
    }
    
    public void id(URI id) {
        this.id = id;
    }

    
    public void holder(URI holder) {
        this.holder = holder;
    }

}
 
