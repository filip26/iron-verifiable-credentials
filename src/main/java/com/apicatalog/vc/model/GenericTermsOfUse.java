package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record GenericTermsOfUse(
        URI id,
        LinkedFragment ld) implements TermsOfUse {

    public static TermsOfUse of(LinkedNode node) throws InvalidSelector {
        return new GenericTermsOfUse(node.asFragment().uri(), node.asFragment());
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }
}
