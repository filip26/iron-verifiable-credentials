package com.apicatalog.cryptosuite.primitive;

import java.nio.charset.StandardCharsets;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.algorithm.CanonicalizationMethod;
import com.apicatalog.jsonld.json.JsonCanonicalizer;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.vc.model.VerifiableMaterial;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class JsonCanonicalizationScheme implements CanonicalizationMethod {

    @Override
    public byte[] canonicalize(VerifiableMaterial material) throws CryptoSuiteError {

        JsonObject compacted = material.compacted();

        if (!compacted.containsKey(Keywords.CONTEXT) && material.context() != null && !material.context().isEmpty()) {
            compacted = Json.createObjectBuilder(compacted).add(Keywords.CONTEXT, material.context().json()).build();
        }

        return JsonCanonicalizer.canonicalize(compacted).getBytes(StandardCharsets.UTF_8);
    }
}
