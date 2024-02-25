package com.apicatalog.vc.issuer;

import java.util.Collection;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.vc.ModelVersion;

import jakarta.json.JsonObject;

public interface ProofDraft {

    /**
     * The proof JSON-LD context URI(s) to compact the proof
     *
     * @param model a credential data model version
     * @return the proof JSON-LD context URI(s)
     */
    Collection<String> context(ModelVersion model);

    /**
     * Returns a {@link CryptoSuite} used to create and to verify the proof value.
     * 
     * @return {@link CryptoSuite} attached to the proof.
     */
    CryptoSuite cryptoSuite();

    JsonObject unsignedCopy();

    JsonObject signedCopy(JsonObject proofValue);
}
