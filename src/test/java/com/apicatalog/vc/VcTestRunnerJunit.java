package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;

public class VcTestRunnerJunit {

    private final VcTestCase testCase;

    public final static DocumentLoader LOADER =
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
        assertNotNull(testCase.type);

        try {
            if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/VerifyTest")) {

                Vc.verify(testCase.input, LOADER);
                
            } else if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/DataIntegrityTest")) {

                final VcDocument vcDocument = VcDocument.load(testCase.input, LOADER);        //TODO use Vc API
                assertNotNull(vcDocument);
                
                
            } else {
                fail("Unknown test execution method");
                return;
            }
 
             if (testCase.type.stream().noneMatch(o -> o.endsWith("PositiveEvaluationTest"))) {
                 fail();
                 return;
             }
             
        } catch (VerificationError e) {
            assertException(e.getCode() != null ? e.getCode().name() : null, e);
            
        } catch (DataIntegrityError e) {
            e.printStackTrace();
            assertException(null, e);
        }
    }
    
    final void assertException(final String code, Throwable e) {

        if (testCase.type.stream().noneMatch(o -> o.endsWith("NegativeEvaluationTest"))) {
            e.printStackTrace();
            fail(e);
            return;
        }

        // compare expected exception
        assertEquals(testCase.result, code);
    }
    
}
