package com.apicatalog.vc.di;

import java.net.URI;

import com.apicatalog.vc.processor.Parameter;

public class DataIntegrityParam {

    protected DataIntegrityParam() {
        // protected static class
    }
    
    public static final Parameter<String> challenge(String challenge) {
        return Parameter.of(VcdiVocab.CHALLENGE.name(), challenge);
    }

    public static final Parameter<String> domain(String domain) {
        return Parameter.of(VcdiVocab.DOMAIN.name(), domain);
    }

    public static final Parameter<URI> purpose(URI purpose) {
        return Parameter.of(VcdiVocab.PURPOSE.name(), purpose);
    }

    public static final Parameter<String> nonce(String nonce) {
        return Parameter.of(VcdiVocab.NONCE.name(), nonce);
    }
}
