package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record GenericRefreshService(
        URI id,
        LinkedFragment ld) implements RefreshService {

    public static RefreshService of(LinkedNode node) throws InvalidSelector {
        return new GenericRefreshService(node.asFragment().uri(), node.asFragment());
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }
}
