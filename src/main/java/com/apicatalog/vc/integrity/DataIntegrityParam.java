package com.apicatalog.vc.integrity;

import com.apicatalog.vc.processor.Parameter;

public class DataIntegrityParam {

    protected DataIntegrityParam() {
        // protected static class
    }
    
    public static final Parameter<String> challenge(String challenge) {
        return Parameter.of(DataIntegrityVocab.CHALLENGE.name(), challenge);
    }

    public static final Parameter<String> domain(String domain) {
        return Parameter.of(DataIntegrityVocab.DOMAIN.name(), domain);
    }

    public static final Parameter<String> purpose(String purpose) {
        return Parameter.of(DataIntegrityVocab.PURPOSE.name(), purpose);
    }

    public static final Parameter<String> nonce(String nonce) {
        return Parameter.of(DataIntegrityVocab.NONCE.name(), nonce);
    }
}
