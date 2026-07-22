package com.apicatalog.trust.lexical;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;

public interface MapProofReader {
    
    boolean isAccepted(Map<String, Object> proof);

    // reads from tree
    Proof read(
            Collection<String> contexts,
            Map<String, Object> proof,
            byte[] proofPayload,
            PayloadGenerator payload);

//    String signatureProperty();

}
