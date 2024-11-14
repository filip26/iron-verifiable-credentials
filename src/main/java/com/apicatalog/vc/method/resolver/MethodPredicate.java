package com.apicatalog.vc.method.resolver;

import java.net.URI;
import java.util.Objects;
import java.util.function.Predicate;

import com.apicatalog.vc.proof.Proof;

public class MethodPredicate {

    public static Predicate<Proof> proofExists = Objects::nonNull;
    
    public static Predicate<Proof> methodExists = proofExists.and(proof -> proof.method() != null);
    
    public static Predicate<Proof> methodIdExists = methodExists.and(proof -> proof.method().id() != null);
    
    public static Predicate<Proof> methodId(Predicate<URI> id) {
        return methodIdExists.and(proof -> id.test(proof.method().id()));
    }
}
