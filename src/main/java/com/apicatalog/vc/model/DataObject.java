package com.apicatalog.vc.model;

import com.apicatalog.oxygen.ld.LinkedObject;

import jakarta.json.JsonObject;

public interface DataObject extends LinkedObject {

//    protected final ModelVersion version;
//
//    protected DataObject(ModelVersion version, JsonObject expanded) {
//        super(expanded);
//        this.version = version;
//    }

    /**
     * Verifiable credentials data model version.
     * 
     * @return the data model version, never <code>null</code>
     */
    ModelVersion version();
//        return version;
//    }
}
