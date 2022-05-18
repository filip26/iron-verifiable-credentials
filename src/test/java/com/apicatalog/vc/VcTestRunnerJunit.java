package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VcTestRunnerJunit {

    private final VcTestCase testCase;

    public VcTestRunnerJunit(VcTestCase testCase) {
        this.testCase = testCase;
    }

    public boolean execute() {

        assertNotNull(testCase.input);

        return Vc.verify(testCase.input);
    }

}
