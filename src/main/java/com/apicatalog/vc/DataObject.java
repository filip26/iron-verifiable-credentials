package com.apicatalog.vc;

import com.apicatalog.ld.LinkedObject;

import jakarta.json.JsonObject;

public class DataObject extends LinkedObject {

    protected final ModelVersion version;
    
    protected DataObject(ModelVersion version, JsonObject expanded) {
        super(expanded);
        this.version = version;
    }
    
    /**
     * Verifiable credentials data model version.
     * 
     * @return the data model version, never <code>null</code>
     */
    public ModelVersion version() {
        return version;
    }
}
