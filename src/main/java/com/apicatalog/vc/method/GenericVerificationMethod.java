package com.apicatalog.vc.method;

import java.net.URI;

import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.linkedtree.LinkedNode;

public record GenericVerificationMethod(
        URI id,
        URI type,
        URI controller,
        LinkedNode ld) implements VerificationMethod {
    
    
}
