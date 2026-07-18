package com.apicatalog.trust.signature;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;

@FunctionalInterface
public interface SignatureDecoder {

    Signature decode(String value, Proof proof, PayloadGenerator payload);

}
