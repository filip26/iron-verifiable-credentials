package com.apicatalog.vc.assertion;

import java.net.URI;

import com.apicatalog.ld.schema.LdTerm;

public class ObjectAssertion extends Assertion {

    public ObjectAssertion(AssertionScope scope) {
        super(scope);
    }
    
    public ObjectAssertion isTypeOf(LdTerm id) {
        return this;
    }

    public ObjectAssertion type(URI id) {
        return this;
    }

    public ValueAssertion property(LdTerm name) {
        return null;
    }

}
