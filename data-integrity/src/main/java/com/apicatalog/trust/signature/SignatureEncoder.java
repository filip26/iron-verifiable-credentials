package com.apicatalog.trust.signature;

@FunctionalInterface
public interface SignatureEncoder {

    String encode(Signature signature);

}
