package com.apicatalog.vc.issuer;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.vcdm.VcdmVersion;

import jakarta.json.JsonObject;

public class IssuerDetails implements Linkable {

    protected IssuerDetails(VcdmVersion version, JsonObject expanded) {
//        super(version, expanded);
    }
    
    public static IssuerDetails of(VcdmVersion version, JsonObject expanded) {
        return null;
    }

    public void validate() throws DocumentError {
        // issuer - @id
//        if (id == null) {
//            throw new DocumentError(ErrorType.Invalid, VcVocab.ISSUER);
//        }
    }

}
