package com.apicatalog.trust.processor;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.ProofCursor;

public interface DocumentProcessor {

//    <T> T adapt(Class<T> type);
//
//    void add(Proof proof);
//
//    void write(TreeEmitter emitter);
//
//    Map<String, Object> toMap();
//
    ProofCursor createProofCursor();
//
    PayloadGenerator createPayload();
//
//    boolean hasProofs();

}
