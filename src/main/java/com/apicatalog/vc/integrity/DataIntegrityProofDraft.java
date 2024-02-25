package com.apicatalog.vc.integrity;

import java.util.Arrays;
import java.util.Collection;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.issuer.ProofDraft;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class DataIntegrityProofDraft implements ProofDraft {

    protected static final Collection<String> V1_CONTEXTS = Arrays.asList(
            "https://w3id.org/security/data-integrity/v2",
            "https://w3id.org/security/multikey/v1");

    protected static final Collection<String> V2_CONTEXTS = Arrays.asList(
            "https://www.w3.org/ns/credentials/v2");

    protected final CryptoSuite crypto;
    protected final JsonObject expanded;

    public DataIntegrityProofDraft(
            CryptoSuite crypto,
            JsonObject expandedProof) {
        this.crypto = crypto;
        this.expanded = expandedProof;
    }

    @Override
    public CryptoSuite cryptoSuite() {
        return crypto;
    }

    @Override
    public Collection<String> context(ModelVersion model) {
        if (ModelVersion.V11.equals(model)) {
            return V1_CONTEXTS;
        }
        return V2_CONTEXTS;
    }

    @Override
    public JsonObject unsignedCopy() {
        return Json.createObjectBuilder(expanded).remove(DataIntegrityVocab.PROOF_VALUE.uri()).build();
    }

    @Override
    public JsonObject signedCopy(JsonObject proofValue) {
        return Json.createObjectBuilder(expanded)
                .add(DataIntegrityVocab.PROOF_VALUE.uri(), Json.createArrayBuilder().add(proofValue))
                .build();
    }
}
