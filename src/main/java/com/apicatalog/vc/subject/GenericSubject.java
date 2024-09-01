package com.apicatalog.vc.subject;

import java.net.URI;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;

public record GenericSubject(
        URI id,
        LinkedFragment fragment) implements Subject {

    @Override
    public LinkedNode ld() {
        return fragment;
    }
}
