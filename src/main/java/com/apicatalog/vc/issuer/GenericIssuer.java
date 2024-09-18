package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record GenericIssuer(
        URI id,
        LinkedNode ld) implements CredentialIssuer {

    public static GenericIssuer of(LinkedNode fragment) throws InvalidSelector {
        return new GenericIssuer(
                fragment.asFragment().uri(),
                fragment);
    }
}
