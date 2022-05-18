package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;

public class VcTestRunnerJunit {

    private final VcTestCase testCase;
    
    private final static DocumentLoader LOADER = 
            new UriBaseRewriter(
                    "https://github.com/filip26/iron-verifiable-credentials/",
                    "classpath:",
                    new SchemeRouter()
                    .set("http", HttpLoader.defaultInstance())
                    .set("https", HttpLoader.defaultInstance())
                    .set("classpath", new ClasspathLoader())
                );

    public VcTestRunnerJunit(VcTestCase testCase) {
        this.testCase = testCase;
    }

    public boolean execute() {

        assertNotNull(testCase.input);

        try {
            return Vc.verify(testCase.input, LOADER);
        } catch (VerificationError e) {
            e.printStackTrace();
        }
        return false;
    }

}
