package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.data.Data;

public interface ProofMapReader {
    
    boolean isAccepted(Map<String, Object> proof);

    // reads from tree
    Proof read(
            Collection<String> contexts,
            Map<String, Object> proof,
            byte[] proofPayload,
            Function<Collection<String>, Data> data);

    String signatureProperty();

}
