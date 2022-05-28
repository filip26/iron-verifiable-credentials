package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.lds.ProofOptions;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519ProofOptions2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;

import jakarta.json.JsonObject;

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

        assertNotNull(testCase.type);
        assertNotNull(testCase.input);

        try {
            if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/tests/VerifyTest")) {

                Vc.verify(testCase.input, LOADER);
                
            } else if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#DataIntegrityTest")) {

                final VcDocument vcDocument = VcDocument.load(testCase.input, LOADER);        //TODO use Vc API
                assertNotNull(vcDocument);
                
            } else if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#IssueTest")) {

                assertNotNull(testCase.result);
                 
                //FIXME
                Ed25519ProofOptions2020 options = new Ed25519ProofOptions2020();
                options.setCreated(Instant.now());
                
                final JsonObject signed = Vc.sign(testCase.input, testCase.keyPair, options, LOADER);
                assertNotNull(signed);
                
                final Document expected = LOADER.loadDocument(URI.create(testCase.result), new DocumentLoaderOptions());
                
                assertEquals(expected.getJsonContent().orElse(null), signed);
                
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
            assertException(null, e);
            
        } catch (JsonLdError e) {
            e.printStackTrace();
            fail(e);
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
