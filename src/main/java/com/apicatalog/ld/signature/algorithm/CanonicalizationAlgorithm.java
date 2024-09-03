package com.apicatalog.ld.signature.algorithm;

import com.apicatalog.ld.signature.LinkedDataSuiteError;
import com.apicatalog.linkedtree.LinkedTree;

/**
 * An algorithm that takes an input document that has more than one possible
 * representation and always transforms it into a canonical form. This process
 * is sometimes also called normalization.
 */
public interface CanonicalizationAlgorithm {

    byte[] canonicalize(LinkedTree tree) throws LinkedDataSuiteError;

}
