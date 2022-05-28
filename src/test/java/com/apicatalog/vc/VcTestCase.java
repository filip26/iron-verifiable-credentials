package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.proof.VerificationKeyReference;
import com.apicatalog.vc.proof.VerificationMethod;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;

public class VcTestCase {

    public URI id;

    public String name;

    public URI input;
    
    public Set<String> type;
    
    public String result;
    
    public URI keyPair;
    
    public VerificationMethod verificationMethod;
    
    public Instant created;

    public static VcTestCase of(JsonObject test, JsonObject manifest, DocumentLoader loader) {

        final VcTestCase testCase = new VcTestCase();

        testCase.id =  URI.create(test.getString(Keywords.ID));

        testCase.type = test.getJsonArray(Keywords.TYPE).stream().map(JsonString.class::cast).map(JsonString::getString).collect(Collectors.toSet());

        testCase.name = test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#name")
                            .getJsonObject(0)
                            .getString(Keywords.VALUE);

        testCase.input = URI.create(test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action")
                            .getJsonObject(0)
                            .getString(Keywords.ID));

        if (test.containsKey("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#result")) {
            final JsonObject result = test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#result").getJsonObject(0);
            testCase.result = result.getString(Keywords.ID, result.getString(Keywords.VALUE, null));
        }

        
        if (test.containsKey("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#options")) {
            JsonObject options = test.getJsonArray("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#options").getJsonObject(0);
            

            testCase.keyPair =
                    URI.create(
                    options.getJsonArray("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#keyPair")
                                .getJsonObject(0)
                                .getString(Keywords.ID));
            
            URI verificationMethod = URI.create(
                    options.getJsonArray("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#verificationMethod")
                    .getJsonObject(0)
                    .getString(Keywords.ID));

            testCase.verificationMethod = new VerificationKeyReference(verificationMethod, loader);
            
            if (options.containsKey("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#created")) {
                testCase.created = Instant.parse(options.getJsonArray("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#created")
                        .getJsonObject(0)
                       .getString(Keywords.VALUE));
            }
            
        }

        return testCase;
    }

    @Override
    public String toString() {
        return id.getFragment() + ": " + name;
    }
}
