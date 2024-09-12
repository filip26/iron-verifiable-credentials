package com.apicatalog.vc.subject;

import java.net.URI;

import com.apicatalog.linkedtree.LinkedNode;

public record GenericSubject(
        URI id,
        LinkedNode ld) implements Subject {

}
