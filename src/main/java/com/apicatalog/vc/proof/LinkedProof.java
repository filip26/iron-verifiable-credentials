package com.apicatalog.vc.proof;

import java.util.Objects;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.di.VcdiVocab;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.ModelAssertions;

@Fragment(generic = true)
public interface LinkedProof extends Proof {

    /**
     * A document to which this proof is bound, linked to.
     * 
     * @return a verifiable document instance
     */
    @Provided
    VerifiableDocument document();

    @Override
    default void verify(VerificationKey key) throws VerificationError, DocumentError {
        Objects.requireNonNull(key);
        ModelAssertions.assertNotNull(this::signature, VcdiVocab.PROOF_VALUE);

        // verify signature
        signature().verify(key);
    }
}
