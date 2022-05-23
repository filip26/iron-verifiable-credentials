package com.apicatalog.vc;

import java.net.URI;

import jakarta.json.JsonObject;

public class VcTestCase {

    public URI id;

    public String name;

    public String input;

    public static VcTestCase of(JsonObject test, JsonObject manifest) {

        final VcTestCase testCase = new VcTestCase();

        testCase.id =  URI.create(test.getString("@id"));

        testCase.name = test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#name")
                            .getJsonObject(0)
                            .getString("@value");

        testCase.input = test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action")
                            .getJsonObject(0)
                            .getString("@id");
        return testCase;
    }

    @Override
    public String toString() {
        return id.getFragment() + ": " + name;
    }
}
