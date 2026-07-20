package com.apicatalog.trust.semantic;

import java.util.Collection;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;

public interface GraphProofReader {

    boolean isAccepted(Collection<String[]> proof);

    // reads from n-quads
    Proof read(
            Collection<String[]> proof,
            SemanticModel model,
            PayloadGenerator payload);
}
