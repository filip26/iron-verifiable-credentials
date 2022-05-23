package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

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

    public void execute() {

        assertNotNull(testCase.input);

        try {

             Vc.verify(testCase.input, LOADER);
             
             if (testCase.type.stream().noneMatch(o -> o.endsWith("PositiveEvaluationTest"))) {
                 fail();
                 return;
             }
             
        } catch (VerificationError | DataIntegrityError e) {
            if (testCase.type.stream().noneMatch(o -> o.endsWith("NegativeEvaluationTest"))) {
                e.printStackTrace();
                fail(e);                
            }
        }
    }

}
