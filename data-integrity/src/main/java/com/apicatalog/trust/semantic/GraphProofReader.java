package com.apicatalog.trust.semantic;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;

//TODO generalize, use generic to parameterize
public interface GraphProofReader {

    boolean isAccepted(Graph.Node proof);

    // reads from n-quads
    Proof read(
            String id,
            Graph proof,
            SemanticModel model,
            PayloadGenerator payload);
}
