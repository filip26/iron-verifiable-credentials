package com.apicatalog.vc.subject;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.vcdm.VcdmVersion;

import jakarta.json.JsonObject;

public class Subject implements Linkable {

    public Subject(VcdmVersion version, JsonObject expanded) {
//        super(version, expanded);
    }

    public void validate() throws DocumentError {
    }

}
