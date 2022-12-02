package com.apicatalog.ld.signature;

public interface SignatureSuiteProvider {

    boolean isSupported(String suiteType);

    // TODO use optional
    SignatureSuite getSignatureSuite(String suiteType);

}
