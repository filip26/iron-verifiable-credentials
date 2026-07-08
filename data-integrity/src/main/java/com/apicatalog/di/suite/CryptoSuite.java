package com.apicatalog.di.suite;

import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public interface CryptoSuite {

    String id();


    String c14n();

    String encode(Signature signature);

    Signature decode(String encoded, Proof proof, Data data);
//TODO replace with    SignatureDecoder decodeSignature(MessageDigest digestor);
//                 default    SignatureDecoder decodeSignature() {
// }    
}
