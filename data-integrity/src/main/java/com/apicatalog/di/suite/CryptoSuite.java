package com.apicatalog.di.suite;

import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public interface CryptoSuite {

    enum Feature {
        TORDF,
        RDFC,
        JCS,
        EXPANSION,
        COMPACTION,
    }
    
    String id();
    String c14n();
    
    Signature decode(String encoded, Proof proof, PayloadProcessor payload);
    
    @Deprecated
    String encode(Signature signature);

}
