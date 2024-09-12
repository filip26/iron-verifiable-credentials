package com.apicatalog.vc.status;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedNode;

public record GenericStatus(LinkedNode ld) implements Status {

    @Override
    public void validate() throws DocumentError {
        throw new UnsupportedOperationException("An uknown status cannot be validated.");
    }

}
