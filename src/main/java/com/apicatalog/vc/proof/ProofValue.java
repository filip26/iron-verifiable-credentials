package com.apicatalog.vc.proof;

import com.apicatalog.controller.key.RawByteKey;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedTree;

public interface ProofValue {

    //TODO pass VerifiableTree instead of LinkedTree, traversal -> conversion
    void verify(
            CryptoSuite crypto,
            LinkedTree data,
            LinkedTree unsignedProof,
            RawByteKey publicKey) throws VerificationError, DocumentError;

    byte[] toByteArray() throws DocumentError;
}
