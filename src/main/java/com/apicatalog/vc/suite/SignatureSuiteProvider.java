package com.apicatalog.vc.suite;

public interface SignatureSuiteProvider {

    boolean isSupported(String suiteType);

    SignatureSuite find(String suiteType);

}
