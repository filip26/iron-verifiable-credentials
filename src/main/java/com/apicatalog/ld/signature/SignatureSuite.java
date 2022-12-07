package com.apicatalog.ld.signature;

import java.net.URI;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.proof.ProofOptions;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public interface SignatureSuite {

    // proof type id
    LdTerm getId();

    // JSON-LD context defining the type
    URI getContext();

    LdSchema getSchema();

    CryptoSuite getCryptoSuite();

    ProofOptions createOptions();
}
