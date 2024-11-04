package com.apicatalog.vc.proof;

import java.util.Objects;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.vc.primitive.VerifiableTree;

public interface VerifiableProof extends Proof {

    @Override
    default void verify(VerificationKey key) throws VerificationError, DocumentError {

//        Objects.requireNonNull(signature);
        Objects.requireNonNull(key);

//        // a data before issuance - no proof attached
//        final LinkedTree unsigned = VerifiableTree.unsigned(verifiable);
//
//        Objects.requireNonNull(unsigned);
//
//        // remove a proof value and get a new unsigned copy
//        final LinkedTree unsignedProof = unsignedProof(((Linkable)this).ld().asFragment().root());

//        DictionaryWriter.writeToStdOut(unsigned);
//        DictionaryWriter.writeToStdOut(unsignedProof);

        // verify signature
        signature().verify(key);
    }
}
