package com.apicatalog.di.suite;

import com.apicatalog.trust.Signature;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;

public interface CryptoSuite {

    String id();

    String algorithm();

    String c14n();

    String digest();

    String encode(Signature signature);

    byte[] decode(String value);

    Signature newSignature(String value, Proof proof, Data data);
}
