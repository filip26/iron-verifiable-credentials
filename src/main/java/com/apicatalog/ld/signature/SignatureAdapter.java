package com.apicatalog.ld.signature;

import java.util.Optional;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.ld.signature.proof.ProofOptions;

import jakarta.json.JsonValue;

//FIXME split into KeyAdapter ProofAdapter
public interface SignatureAdapter {

    Optional<VerificationKey> materializeKey(JsonValue value) throws DataError;

    Optional<KeyPair> materializeKeyPair(JsonValue value) throws DataError;

    Optional<SignatureSuite> findSuiteByType(String type) throws DataError;

    Optional<Proof> materialize(ProofOptions options) throws DataError;

    Optional<Proof> materializeProof(JsonValue value, DocumentLoader loader) throws DataError;

    boolean isSupportedType(String type);
}
