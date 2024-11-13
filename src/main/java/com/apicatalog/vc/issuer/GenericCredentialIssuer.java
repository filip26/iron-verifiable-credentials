package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vc.CredentialIssuer;

public record GenericCredentialIssuer(
        URI id,
        LinkedNode ld) implements CredentialIssuer {

    public static GenericCredentialIssuer of(LinkedNode fragment) throws NodeAdapterError {
        return new GenericCredentialIssuer(
                fragment.asFragment().uri(),
                fragment);
    }
}
