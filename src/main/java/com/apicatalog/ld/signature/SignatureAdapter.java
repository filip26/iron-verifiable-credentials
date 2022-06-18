package com.apicatalog.ld.signature;

import java.util.Optional;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.EmbeddedProof;
import com.apicatalog.ld.signature.proof.ProofOptions;

import jakarta.json.JsonValue;

public interface SignatureAdapter {

    Optional<VerificationKey> materializeKey(JsonValue value) throws DataError;

    Optional<KeyPair> materializeKeyPair(JsonValue value) throws DataError;

    Optional<SignatureSuite> getSuiteByType(String type) throws DataError;

    Optional<EmbeddedProof> materialize(ProofOptions options) throws DataError;

    Optional<EmbeddedProof> materializeProof(JsonValue value, DocumentLoader loader) throws DataError;
}
