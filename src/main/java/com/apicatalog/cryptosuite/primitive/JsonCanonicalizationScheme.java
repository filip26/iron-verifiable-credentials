package com.apicatalog.cryptosuite.primitive;

import java.nio.charset.StandardCharsets;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.algorithm.Canonicalizer;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.linkedtree.json.JsonCanonicalizer;
import com.apicatalog.vc.model.VerifiableMaterial;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class JsonCanonicalizationScheme implements Canonicalizer {

    @Override
    public byte[] canonicalize(VerifiableMaterial material) throws CryptoSuiteError {

        JsonObject compacted = material.compacted();

        if (!compacted.containsKey(Keywords.CONTEXT)) {
            compacted = Json.createObjectBuilder(compacted).add(Keywords.CONTEXT, Json.createArrayBuilder(material.context())).build();
        }

        return JsonCanonicalizer.canonicalize(compacted).getBytes(StandardCharsets.UTF_8);
    }
}
