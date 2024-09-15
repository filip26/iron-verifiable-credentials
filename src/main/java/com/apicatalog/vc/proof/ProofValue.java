package com.apicatalog.vc.proof;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.linkedtree.LinkedTree;

public interface ProofValue {

    //TODO pass VerifiableTree instead of LinkedTree, traversal -> conversion
    void verify(
            CryptoSuite crypto,
            LinkedTree data,
            LinkedTree unsignedProof,
            byte[] publicKey) throws VerificationError, DocumentError;

    byte[] toByteArray() throws DocumentError;
}
