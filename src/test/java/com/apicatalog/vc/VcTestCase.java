package com.apicatalog.vc;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;

public class VcTestCase {

    public URI id;

    public String name;

    public URI input;
    
    public Set<String> type;

    public static VcTestCase of(JsonObject test, JsonObject manifest) {

        final VcTestCase testCase = new VcTestCase();

        testCase.id =  URI.create(test.getString("@id"));

        testCase.type = test.getJsonArray("@type").stream().map(JsonString.class::cast).map(JsonString::getString).collect(Collectors.toSet());

        testCase.name = test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#name")
                            .getJsonObject(0)
                            .getString("@value");

        testCase.input = URI.create(test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action")
                            .getJsonObject(0)
                            .getString("@id"));
                
        return testCase;
    }

    @Override
    public String toString() {
        return id.getFragment() + ": " + name;
    }
}
