package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.reader.LinkedReaderError;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class VcTestCase {

    static final String BASE = "https://github.com/filip26/iron-verifiable-credentials/";

    public URI id;

    public String name;

    public URI input;

    public Set<String> type;

    public Object result;

    public URI keyPair;

    public VerificationMethod verificationMethod;

    public Instant created;

    public String domain;

    public String challenge;

    public String nonce;

    public String purpose;

    public URI context;

    public boolean compacted;

    public static VcTestCase of(JsonObject test, JsonObject manifest) {

        final VcTestCase testCase = new VcTestCase();

        testCase.id = URI.create(test.getString(Keywords.ID));

        testCase.type = test.getJsonArray(Keywords.TYPE)
                .stream()
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .collect(Collectors.toSet());

        testCase.name = test.getJsonArray(da("name")).getJsonObject(0).getString(Keywords.VALUE);

        testCase.input = URI.create(test.getJsonArray(da("action"))
                .getJsonObject(0)
                .getString(Keywords.ID));

        if (test.containsKey(vocab("context"))) {
            testCase.context = URI.create(test
                    .getJsonArray(vocab("context"))
                    .getJsonObject(0)
                    .getString(Keywords.ID));
        }

        testCase.compacted = test.containsKey(vocab("compacted"))
                && test.getJsonArray(vocab("compacted"))
                        .getJsonObject(0)
                        .getBoolean(Keywords.VALUE, false);

        if (test.containsKey(da("result"))) {
            final JsonObject result = test.getJsonArray(da("result")).getJsonObject(0);

            JsonValue resultValue = result.getOrDefault(Keywords.ID, result.getOrDefault(Keywords.VALUE, null));

            if (JsonUtils.isString(resultValue)) {
                testCase.result = ((JsonString) resultValue).getString();

            } else {
                testCase.result = !JsonValue.ValueType.FALSE.equals(resultValue.getValueType());
            }
        }

        if (test.containsKey(vocab("options"))) {

            final JsonObject options = test.getJsonArray(vocab("options")).getJsonObject(0);

            if (options.containsKey(vocab("keyPair"))) {
                testCase.keyPair = URI.create(options.getJsonArray(vocab("keyPair"))
                        .getJsonObject(0).getString(Keywords.ID));
            }

            if (options.containsKey(vocab("verificationMethod"))) {

                final JsonObject method = options.getJsonArray(vocab("verificationMethod"))
                        .getJsonObject(0);

                try {
                    JsonLdTreeReader reader = JsonLdTreeReader.with(

                    );

                    testCase.verificationMethod = reader.readExpanded(method)
                            .cast(VerificationMethod.class);

                } catch (LinkedReaderError e) {
                    fail(e);
                }
            }

            if (options.containsKey(vocab("created"))) {
                testCase.created = Instant.parse(options.getJsonArray(vocab("created"))
                        .getJsonObject(0).getString(Keywords.VALUE));
            }

            if (options.containsKey(vocab("domain"))) {
                testCase.domain = options.getJsonArray(vocab("domain")).getJsonObject(0)
                        .getString(Keywords.VALUE);
            }

            if (options.containsKey(vocab("challenge"))) {
                testCase.challenge = options.getJsonArray(vocab("challenge")).getJsonObject(0)
                        .getString(Keywords.VALUE);
            }

            if (options.containsKey(vocab("nonce"))) {
                testCase.nonce = options.getJsonArray(vocab("nonce")).getJsonObject(0)
                        .getString(Keywords.VALUE);
            }

            if (options.containsKey(vocab("purpose"))) {
                testCase.purpose = options.getJsonArray(vocab("purpose")).getJsonObject(0)
                        .getString(Keywords.VALUE);
            }
        }

        return testCase;
    }

    @Override
    public String toString() {
        return id.getFragment() + ": " + name;
    }

    static String base(String url) {
        return BASE.concat(url);
    }

    static String da(String term) {
        return "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#".concat(term);
    }

    static String vocab(String term) {
        return base("tests/vocab#").concat(term);
    }
}
