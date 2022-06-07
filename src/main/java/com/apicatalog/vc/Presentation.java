package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;

import jakarta.json.JsonValue;

public class Presentation implements Verifiable {

    public static final String TYPE_VALUE = "https://www.w3.org/2018/credentials#VerifiablePresentation";

    public static boolean isPresentation(JsonValue value) {
        if (value == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }

        return JsonUtils.isObject(value) && JsonLdUtils.isTypeOf(TYPE_VALUE, value.asJsonObject());
    }

    @Override
    public boolean isPresentation() {
        return true;
    }

    @Override
    public Presentation asPresentation() {
        return this;
    }
}
