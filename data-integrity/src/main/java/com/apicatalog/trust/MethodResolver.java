package com.apicatalog.trust;

import com.apicatalog.trust.proof.Proof;

@FunctionalInterface
public interface MethodResolver {

    byte[] resolve(Proof proof);
    
}
