package com.apicatalog.vc.subject;

import java.net.URI;

import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;

public record GenericSubject(
        URI id,
        LinkedNode ld) implements Subject {

    public static GenericSubject of(LinkedNode fragment) throws NodeAdapterError {
        return new GenericSubject(
                fragment.asFragment().uri(),
                fragment);
    }

}
