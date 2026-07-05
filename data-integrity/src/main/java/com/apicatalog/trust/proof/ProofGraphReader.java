package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.function.Function;

import com.apicatalog.trust.data.Data;

public interface ProofGraphReader {

    boolean isAccepted(Collection<String[]> proof);

    // reads from n-quads
    Proof read(
            Collection<String[]> proof,
            Function<Collection<String>, Data> data);

}
