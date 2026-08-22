package com.apicatalog.trust.lexical;

import java.util.Map;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;

public interface PropertyProofMapper {
    
    boolean accepts(Map<String, Object> proof);

    // reads from tree
    Proof materialize(
            Map<String, Object> proof,
            LexicalModel model,
            PayloadGenerator payload);

//    String signatureProperty();

}
