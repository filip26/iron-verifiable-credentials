package com.apicatalog.trust.proof;

import java.util.Collection;

public interface GraphProofReader {

    boolean isAccepted(Collection<String[]> proof);

    // reads from n-quads
    Proof read(
            Collection<String[]> proof,
            GraphProofCursor cursor);
}
