package com.apicatalog.vc.proof;

import jakarta.json.JsonObject;

public interface DerivedProof extends Proof {

    JsonObject document();
    
}
