package com.apicatalog.trust.signature;

import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;

public interface Signature {

    Data data();

    Proof proof();

    byte[] toByteArray();

    String algorithm();
}
