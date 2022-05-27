package com.apicatalog.lds;

import jakarta.json.JsonObject;

/**
 * An algorithm that takes an input document that has more than one possible representation
 * and always transforms it into a canonical form. 
 * This process is sometimes also called normalization.
 */
public interface CanonicalizationAlgorithm {

    byte[] canonicalize(JsonObject proof);

}
