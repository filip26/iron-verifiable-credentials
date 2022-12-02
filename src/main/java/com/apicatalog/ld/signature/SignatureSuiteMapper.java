package com.apicatalog.ld.signature;

import java.util.LinkedHashMap;

public final class SignatureSuiteMapper extends LinkedHashMap<String, SignatureSuite> implements SignatureSuiteProvider {

    private static final long serialVersionUID = -8895021841340234772L;

    @Override
    public boolean isSupported(String suiteType) {
        return containsKey(suiteType);
    }

    @Override
    public SignatureSuite getSignatureSuite(String suiteType) {
        return get(suiteType);
    }

    /**
     * Add a new signature suite. An existing suite of the same type is replaced.
     * 
     * @param suite a suite to add
     * @return the processor instance
     */
    public SignatureSuiteMapper add(final SignatureSuite suite) {
        if (suite == null) {
            throw new IllegalArgumentException("The 'suite' paramenter must not be null.");
        }

        put(suite.getProofType().id().toString(), suite);
        return this;
    }
}
