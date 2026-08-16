package com.apicatalog.trust.semantic;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;

public interface GraphProofMapper {

    boolean accepts(Graph.Node node);

    // reads from n-quads
    Proof materialize(
            String id,
            Graph graph,
            SemanticModel model,
            PayloadGenerator payload);
}
