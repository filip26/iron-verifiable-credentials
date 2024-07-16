package com.apicatalog.vc.status;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.DataObject;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.VcVocab;

import jakarta.json.JsonObject;

public class Status extends DataObject {

    public Status(ModelVersion version, JsonObject expanded) {
        super(version, expanded);
    }
    
    public void validate() throws DocumentError {
        // @type is required when status is present
        if (ModelVersion.V20.equals(version)
                && (type != null
                || type.isEmpty())
                ) {
            throw new DocumentError(ErrorType.Invalid, VcVocab.STATUS);
        }
    }
}
