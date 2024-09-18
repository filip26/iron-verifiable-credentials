package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record GenericStatus(URI id, LinkedNode ld) implements Status {

    public static Status of(LinkedNode node) throws InvalidSelector {
        return new GenericStatus(node.asFragment().uri(), node);
    }
    
    @Override
    public void validate() throws DocumentError {
        throw new UnsupportedOperationException("An uknown status cannot be validated.");
    }
    
    @Override
    public Collection<String> type() {
        return ld.asFragment().type().stream().toList();
    }

}
