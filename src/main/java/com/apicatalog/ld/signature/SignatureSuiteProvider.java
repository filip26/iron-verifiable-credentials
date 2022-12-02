package com.apicatalog.ld.signature;

public interface SignatureSuiteProvider {

    boolean isSupported(String suiteType);

    SignatureSuite find(String suiteType);

}
