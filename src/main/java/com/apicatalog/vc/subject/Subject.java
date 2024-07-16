package com.apicatalog.vc.subject;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.DataObject;
import com.apicatalog.vc.ModelVersion;

import jakarta.json.JsonObject;

public class Subject extends DataObject {

    public Subject(ModelVersion version, JsonObject expanded) {
        super(version, expanded);
    }

    public void validate() throws DocumentError {
    }

}
