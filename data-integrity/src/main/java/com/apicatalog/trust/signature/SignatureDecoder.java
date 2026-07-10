package com.apicatalog.trust.signature;

import com.apicatalog.trust.payload.PayloadSelector;
import com.apicatalog.trust.proof.Proof;

@FunctionalInterface
public interface SignatureDecoder {

    Signature decode(String value, Proof proof, PayloadSelector payload);

}
