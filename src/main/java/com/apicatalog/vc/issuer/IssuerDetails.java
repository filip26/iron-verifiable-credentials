package com.apicatalog.vc.issuer;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;

public interface IssuerDetails extends Linkable {

//    protected IssuerDetails(VcdmVersion version, JsonObject expanded) {
////        super(version, expanded);
//    }
//    
//    public static IssuerDetails of(VcdmVersion version, JsonObject expanded) {
//        return null;
//    }

    void validate() throws DocumentError;
        // issuer - @id
//        if (id == null) {
//            throw new DocumentError(ErrorType.Invalid, VcVocab.ISSUER);
//        }
//    }

}
