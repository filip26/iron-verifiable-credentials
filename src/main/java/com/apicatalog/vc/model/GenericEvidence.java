package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record GenericEvidence(
        URI id,
        LinkedFragment ld) implements Evidence {

    public static Evidence of(LinkedNode node) throws InvalidSelector {
        return new GenericEvidence(node.asFragment().uri(), node.asFragment());
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }
}
