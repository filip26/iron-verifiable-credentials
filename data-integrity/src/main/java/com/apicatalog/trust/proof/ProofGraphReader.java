package com.apicatalog.trust.proof;

import java.util.Collection;

public interface ProofGraphReader extends ProofReader {

//    @FunctionalInterface
//    interface Supplier {
//        ProofGraphReader newInstance(
//                Collection<String[]> proof, 
//                ProofGraphCursor cursor);
//    }
    
    boolean isAccepted(Collection<String[]> proof);

    // reads from n-quads
    Proof read(
            Collection<String[]> proof,
            ProofGraphCursor cursor);
    
//
}
