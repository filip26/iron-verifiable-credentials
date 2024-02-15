package com.apicatalog.vc.integrity;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.model.Proof;

import jakarta.json.JsonObject;

class DataIntegrityProofReader {

    public static final Proof read(
            JsonObject document,
            DataIntegritySuite suite) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final LdNode node = LdNode.of(document);

        String cryptoSuiteName = node.scalar(DataIntegrityVocab.CRYPTO_SUITE).string();

        byte[] proofValue = node.scalar(DataIntegrityVocab.PROOF_VALUE).multibase(Multibase.BASE_58_BTC);
        
        CryptoSuite crypto = suite.getCryptoSuite(cryptoSuiteName, proofValue);

        DataIntegrityProof proof = new DataIntegrityProof(suite, crypto, document);

        proof.id = node.id();

        proof.created = node.scalar(DataIntegrityVocab.CREATED).xsdDateTime();

        proof.purpose = node.node(DataIntegrityVocab.PURPOSE).id();

        proof.domain = node.scalar(DataIntegrityVocab.DOMAIN).string();

        proof.challenge = node.scalar(DataIntegrityVocab.CHALLENGE).string();

        proof.nonce = node.scalar(DataIntegrityVocab.NONCE).string();

        proof.method = node.node(DataIntegrityVocab.VERIFICATION_METHOD).map(suite.methodAdapter);

        proof.value = proofValue;

        proof.previousProof = node.node(DataIntegrityVocab.PREVIOUS_PROOF).id();

        return proof;
    }
}
