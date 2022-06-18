package com.apicatalog.did;

import java.net.URI;

import com.apicatalog.multicodec.Multicodec.Codec;

public class DidKeyTestCase {

    URI uri;
    boolean negative;
    Codec codec;
    int keyLength;
    String version;

    static DidKeyTestCase create(String uri, Codec codec, int length) {
        return create(uri, codec, length, "1");
    }

    static DidKeyTestCase create(String uri, Codec codec, int length, String version) {
        DidKeyTestCase testCase = new DidKeyTestCase();
        testCase.uri = URI.create(uri);
        testCase.negative = false;
        testCase.codec = codec;
        testCase.keyLength = length;
        testCase.version = version;

        return testCase;

    }

    static DidKeyTestCase create(String uri) {
        DidKeyTestCase testCase = new DidKeyTestCase();
        testCase.uri = uri != null ? URI.create(uri) : null ;
        testCase.negative = true;

        return testCase;
    }

}
