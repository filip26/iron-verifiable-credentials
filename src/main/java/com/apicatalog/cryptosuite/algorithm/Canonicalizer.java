package com.apicatalog.cryptosuite.algorithm;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.vc.model.VerifiableMaterial;

/**
 * An algorithm that takes an input document that has more than one possible
 * representation and always transforms it into a canonical form. This process
 * is sometimes also called normalization.
 */
public interface Canonicalizer {

    byte[] canonicalize(VerifiableMaterial material) throws CryptoSuiteError;

}
