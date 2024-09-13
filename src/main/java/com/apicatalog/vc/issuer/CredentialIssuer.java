package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;

public interface CredentialIssuer extends Linkable {

    URI id();
    
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
