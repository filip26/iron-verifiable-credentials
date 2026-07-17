package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.processor.PayloadProcessor;

public interface MapProofReader {
    
    boolean isAccepted(Map<String, Object> proof);

    // reads from tree
    Proof read(
            Collection<String> contexts,
            Map<String, Object> proof,
            byte[] proofPayload,
            PayloadProcessor payload);

//    String signatureProperty();

}
