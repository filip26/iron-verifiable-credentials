package com.apicatalog.trust.proof;

import java.util.Map;

public interface ProofWriter {

    Map<String, ?> write(Proof proof);
    
}
