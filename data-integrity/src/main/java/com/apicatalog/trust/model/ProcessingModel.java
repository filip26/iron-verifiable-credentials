package com.apicatalog.trust.model;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.processor.DocumentProcessor;
import com.apicatalog.trust.proof.ProofCursor;

public interface ProcessingModel {

    static final String C14N_RDFC = "RDFC";
    static final String C14N_JCS = "JCS";

//    String c14n();

//    ProofCursor createProofCursor(Collection<String> context, Map<String, Object> document);
    
//    ProofCursor createProofCursor(Collection<String> context, Map<String, Object> document);

    DocumentProcessor createProcessor(Map<String, Object> document);

    // TODO accepted proof types, for configuration dump

    // TODO proof predicate or selector returns proof graph or null
    // Function<String[], String>;

}
