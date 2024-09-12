package com.apicatalog.vc.issuer;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedNode;

public record GenericIssuer(
        LinkedNode ld
        ) implements IssuerDetails {

    @Override
    public void validate() throws DocumentError {
        //TODO validate URI presence
    }

}
