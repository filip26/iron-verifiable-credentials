package com.apicatalog.vc.suite;

import java.util.Optional;

import jakarta.json.JsonObject;

public interface SignatureSuiteProvider {

    Optional<SignatureSuite> find(String proofType, JsonObject expandedProof);

}
