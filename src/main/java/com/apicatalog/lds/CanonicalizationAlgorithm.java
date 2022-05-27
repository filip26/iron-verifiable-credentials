package com.apicatalog.lds;

import jakarta.json.JsonObject;

public interface CanonicalizationAlgorithm {

    byte[] canonicalize(JsonObject proof);

}
