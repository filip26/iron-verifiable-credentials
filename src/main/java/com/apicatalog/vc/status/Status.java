package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;

public interface Status extends Linkable {

//    public Status(VcdmVersion version, JsonObject expanded) {
////        super(version, expanded);
//    }
    
    URI id();
    
    Collection<String> type();
//    
    void validate() throws DocumentError;
        // @type is required when status is present
//        if (ModelVersion.V20.equals(version)
//                && (type != null
//                || type.isEmpty())
//                ) {
//            throw new DocumentError(ErrorType.Invalid, VcVocab.STATUS);
//        }
//    }
}
