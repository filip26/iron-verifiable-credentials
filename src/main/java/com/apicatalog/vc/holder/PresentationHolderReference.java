package com.apicatalog.vc.holder;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record PresentationHolderReference(
        URI id,
        LinkedTree root) implements PresentationHolder {

    public static PresentationHolderReference of(LinkedNode node) throws InvalidSelector {
        return new PresentationHolderReference(node.asFragment().uri(), node.root());
    }

    @Override
    public void validate() throws DocumentError {
        // TODO Auto-generated method stub
        // must exists
    }

    public LinkedTree root() {
        return root;
    }

}
