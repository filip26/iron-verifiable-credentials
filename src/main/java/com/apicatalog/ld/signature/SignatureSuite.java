package com.apicatalog.ld.signature;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.ld.signature.proof.ProofType;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public interface SignatureSuite {

    LdSchema getSchema();

    ProofType getProofType();

    CryptoSuite getCryptoSuite();

    ProofOptions createOptions();
}
