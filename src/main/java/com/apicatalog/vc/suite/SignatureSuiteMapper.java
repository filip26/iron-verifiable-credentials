package com.apicatalog.vc.suite;

import java.util.LinkedHashMap;
import java.util.Optional;

import jakarta.json.JsonObject;

@Deprecated
public final class SignatureSuiteMapper extends LinkedHashMap<String, SignatureSuite> implements SignatureSuiteProvider {

    private static final long serialVersionUID = -5966318177536751280L;

    @Override
    public Optional<SignatureSuite> find(String proofType, JsonObject expandedProof) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

//    @Override
//    public boolean isSupported(String suiteType) {
//        return containsKey(suiteType);
//    }
//
//    @Override
//    public SignatureSuite find(String suiteType) {
//        return get(suiteType);
//    }
//
//    /**
//     * Add a new signature suite. An existing suite of the same type is replaced.
//     * 
//     * @param suite a suite to add
//     * @return the processor instance
//     */
//    public SignatureSuiteMapper add(final SignatureSuite suite) {
//        if (suite == null) {
//            throw new IllegalArgumentException("The 'suite' paramenter must not be null.");
//        }
//
//        put(suite.id(), suite);
//        return this;
//    }
}
