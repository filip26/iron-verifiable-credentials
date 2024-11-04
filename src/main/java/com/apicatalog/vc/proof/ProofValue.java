package com.apicatalog.vc.proof;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.ld.DocumentError;

public interface ProofValue {

//    //TODO pass VerifiableTree instead of LinkedTree, traversal -> conversion
//    void verify(
//            CryptoSuite crypto,
//            LinkedTree data,
//            LinkedTree unsignedProof,
//            RawByteKey publicKey) throws VerificationError, DocumentError;

    void verify(VerificationKey key) throws VerificationError, DocumentError;

    byte[] toByteArray() throws DocumentError;
}
