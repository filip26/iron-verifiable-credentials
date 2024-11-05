package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;

public record GenericStatus(URI id, LinkedNode ld) implements Status {

    public static Status of(LinkedNode node) throws NodeAdapterError {
        return new GenericStatus(node.asFragment().uri(), node);
    }

    @Override
    public Collection<String> type() {
        return ld.asFragment().type().stream().toList();
    }
}
