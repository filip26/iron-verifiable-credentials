package com.apicatalog.vc.integrity;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.model.Proof;

import jakarta.json.JsonObject;

public class DataIntegrityProofReader {

    public static final Proof read(
            JsonObject document,
            DataIntegritySuite suite) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        LdNode node = new LdNode(document);

        String cryptoSuiteName = node.get(DataIntegrityVocab.CRYPTO_SUITE)
                .scalar().string();

        CryptoSuite crypto = suite.getCryptoSuite(cryptoSuiteName);

        DataIntegrityProof proof = new DataIntegrityProof(suite, crypto, document);

        proof.id = node.id();

        proof.created = node.get(DataIntegrityVocab.CREATED).scalar().xsdDateTime();

        proof.purpose = node.get(DataIntegrityVocab.PURPOSE).id();

        proof.domain = node.get(DataIntegrityVocab.DOMAIN)
                .scalar().string();

        proof.challenge = node.get(DataIntegrityVocab.CHALLENGE)
                .scalar().string();

        proof.method = node.get(DataIntegrityVocab.VERIFICATION_METHOD).node().map(suite.methodAdapter);

        proof.value = node.get(DataIntegrityVocab.PROOF_VALUE)
                .scalar().multibase(Multibase.BASE_58_BTC);

        return proof;
    }
}
