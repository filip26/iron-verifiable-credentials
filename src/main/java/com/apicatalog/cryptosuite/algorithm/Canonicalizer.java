package com.apicatalog.cryptosuite.algorithm;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.linkedtree.LinkedTree;

/**
 * An algorithm that takes an input document that has more than one possible
 * representation and always transforms it into a canonical form. This process
 * is sometimes also called normalization.
 */
public interface Canonicalizer {

    byte[] canonicalize(LinkedTree tree) throws CryptoSuiteError;

}
