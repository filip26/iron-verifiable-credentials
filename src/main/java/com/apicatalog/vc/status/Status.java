package com.apicatalog.vc.status;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.vcdm.VcdmVersion;

import jakarta.json.JsonObject;

public class Status implements Linkable {

    public Status(VcdmVersion version, JsonObject expanded) {
//        super(version, expanded);
    }
    
    public void validate() throws DocumentError {
        // @type is required when status is present
//        if (ModelVersion.V20.equals(version)
//                && (type != null
//                || type.isEmpty())
//                ) {
//            throw new DocumentError(ErrorType.Invalid, VcVocab.STATUS);
//        }
    }
}
