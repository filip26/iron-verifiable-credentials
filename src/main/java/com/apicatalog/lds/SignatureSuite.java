package com.apicatalog.lds;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization algorithm,
 * a message digest algorithm, and a signature algorithm that are bundled together 
 * for the purposes of safety and convenience.
 */
public interface SignatureSuite extends CanonicalizationAlgorithm, DigestAlgorithm, SignatureAlgorithm {

}
