package com.apicatalog.ld.signature.algorithm;

import com.apicatalog.ld.signature.DataError;

import jakarta.json.JsonStructure;

/**
 * An algorithm that takes an input document that has more than one possible representation
 * and always transforms it into a canonical form.
 * This process is sometimes also called normalization.
 */
public interface CanonicalizationAlgorithm {

    byte[] canonicalize(JsonStructure json) throws DataError;

}
