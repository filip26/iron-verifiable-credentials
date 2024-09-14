package com.apicatalog.ld.signature;

import java.net.URI;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record GenericVerificationMethod(
        URI id,
        URI type,
        URI controller        
        ) implements VerificationMethod {

    public static VerificationMethod of(LinkedFragment source) throws InvalidSelector {        
        return new GenericVerificationMethod(
                source.uri(),
                null,//FIXME
                null
                );
    }
}
