package com.apicatalog.trust.model;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.proof.ProofCursor;

public interface Model {

    ProofCursor createCursor(Collection<String> context, Map<String, Object> document);

    String c14n();
    
    //TODO accepted proof types, for configuration dump
}
