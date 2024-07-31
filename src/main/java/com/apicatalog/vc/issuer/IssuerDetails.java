package com.apicatalog.vc.issuer;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.model.VerifiableObject;
import com.apicatalog.vc.model.ModelVersion;

import jakarta.json.JsonObject;

public class IssuerDetails  {

    protected IssuerDetails(ModelVersion version, JsonObject expanded) {
//        super(version, expanded);
    }
    
    public static IssuerDetails of(ModelVersion version, JsonObject expanded) {
        return null;
    }

    public void validate() throws DocumentError {
        // issuer - @id
//        if (id == null) {
//            throw new DocumentError(ErrorType.Invalid, VcVocab.ISSUER);
//        }
    }

}
