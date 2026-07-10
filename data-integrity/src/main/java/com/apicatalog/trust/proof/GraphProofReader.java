package com.apicatalog.trust.proof;

import java.util.Collection;

import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.payload.PayloadSelector;

public interface GraphProofReader {

    boolean isAccepted(Collection<String[]> proof);

    // reads from n-quads
    Proof read(
            Collection<String[]> proof,
            SemanticModel model,
            PayloadSelector payload);
}
