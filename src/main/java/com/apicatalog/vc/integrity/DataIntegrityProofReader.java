package com.apicatalog.vc.integrity;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdAdapter;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.model.ProofValueProcessor;

import jakarta.json.JsonObject;

public class DataIntegrityProofReader {

    public static final Proof read(
            JsonObject document,
            DataIntegritySuite suite,
            MethodAdapter methodAdapter,
            LdAdapter<byte[]> valueAdapter
            ) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        LdNode node = new LdNode(document);

        String cryptoSuiteName = node.get(DataIntegrityVocab.CRYPTO_SUITE)
                .scalar().string();

        CryptoSuite crypto = suite.getCryptoSuite(cryptoSuiteName);

        DataIntegrityProof proof = new DataIntegrityProof(suite, crypto, document);

        proof.id = node.id();
        
        proof.created = node.get(DataIntegrityVocab.CREATED)
                .required().scalar().xsdDateTime();

        proof.purpose = node.get(DataIntegrityVocab.PURPOSE)
                .required().scalar().link();

        proof.domain = node.get(DataIntegrityVocab.DOMAIN)
                .scalar().string();

        proof.challenge = node.get(DataIntegrityVocab.CHALLENGE)
                .scalar().string();

        proof.method = node.get(DataIntegrityVocab.VERIFICATION_METHOD)
                .required().node().map(methodAdapter);
        
//FIXME        proof.value = node.get(DataIntegrityVocab.PROOF_VALUE)
//                .required().node().map(valueAdapter);
        
        return proof;
    }
}
