package com.apicatalog.vc.issuer;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.DataObject;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.VcVocab;

import jakarta.json.JsonObject;

public class IssuerDetails extends DataObject {

    public IssuerDetails(ModelVersion version, JsonObject expanded) {
        super(version, expanded);
    }

    public void validate() throws DocumentError {
        // issuer - @id
        if (id == null) {
            throw new DocumentError(ErrorType.Invalid, VcVocab.ISSUER);
        }
    }

}
