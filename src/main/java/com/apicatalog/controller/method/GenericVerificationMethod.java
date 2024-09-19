package com.apicatalog.controller.method;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record GenericVerificationMethod(
        URI id,
        String type,
        URI controller,
        Instant revoked,
        LinkedFragment ld
        ) implements VerificationMethod {

    public static VerificationMethod of(LinkedNode source) throws InvalidSelector {        
        return new GenericVerificationMethod(
                source.asFragment().uri(),
                null,//FIXM
                null,
                null,
                source.asFragment()
                );
    }
}
