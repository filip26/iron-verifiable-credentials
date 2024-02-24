package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

public abstract class DataIntegritySuite<S extends ProofValue> implements SignatureSuite {

    protected static final String PROOF_TYPE_NAME = "DataIntegrityProof";

    protected static final String PROOF_TYPE_ID = VcVocab.SECURITY_VOCAB + PROOF_TYPE_NAME;

    protected final MethodAdapter methodAdapter;

    protected final String cryptosuite;

    protected DataIntegritySuite(final String cryptosuite, final MethodAdapter method) {
        this.cryptosuite = cryptosuite;
        this.methodAdapter = method;
    }

    @Override
    public boolean isSupported(String proofType, JsonObject expandedProof) {
        return PROOF_TYPE_ID.equals(proofType) && cryptosuite.equals(getCryptoSuiteName(expandedProof));
    }

    @Override
    public DataIntegrityProof<S> getProof(JsonObject expandedProof) throws DocumentError {

        if (expandedProof == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final LdNode node = LdNode.of(expandedProof);

        final String cryptoSuiteName = node.scalar(DataIntegrityVocab.CRYPTO_SUITE).string();

        final S proofValue = createProofValue();
        
        proofValue.set(node.scalar(DataIntegrityVocab.PROOF_VALUE));

        CryptoSuite crypto = getCryptoSuite(cryptoSuiteName, proofValue);

        final DataIntegrityProof<S> proof = new DataIntegrityProof<>(this, crypto, expandedProof);

        proof.value = proofValue;

        proof.id = node.id();

        proof.created = node.scalar(DataIntegrityVocab.CREATED).xsdDateTime();

        proof.purpose = node.node(DataIntegrityVocab.PURPOSE).id();

        proof.domain = node.scalar(DataIntegrityVocab.DOMAIN).string();

        proof.challenge = node.scalar(DataIntegrityVocab.CHALLENGE).string();

        proof.nonce = node.scalar(DataIntegrityVocab.NONCE).string();

        proof.method = node.node(DataIntegrityVocab.VERIFICATION_METHOD).map(methodAdapter);

        proof.previousProof = node.node(DataIntegrityVocab.PREVIOUS_PROOF).id();

        return proof;
    }

    protected abstract S createProofValue();

    protected abstract CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError;

    protected static String getCryptoSuiteName(final JsonObject proof) {
        if (proof == null) {
            throw new IllegalArgumentException("expandedProof property must not be null.");
        }

        try {
            return LdNode.of(proof).scalar(DataIntegrityVocab.CRYPTO_SUITE).string();

        } catch (DocumentError e) {

        }
        return null;
    }

    protected DataIntegrityProof<S> createDraft(
            CryptoSuite crypto,
            VerificationMethod method,
            URI purpose,
            Instant created,
            String domain,
            String challenge,
            String nonce
            ) throws DocumentError {

        final LdNodeBuilder builder = new LdNodeBuilder();

        builder.type(PROOF_TYPE_ID);
        builder.set(DataIntegrityVocab.CRYPTO_SUITE).scalar("https://w3id.org/security#cryptosuiteString", cryptosuite);
        builder.set(DataIntegrityVocab.VERIFICATION_METHOD).map(methodAdapter, method);
        builder.set(DataIntegrityVocab.CREATED).xsdDateTime(created != null ? created : Instant.now());
        builder.set(DataIntegrityVocab.PURPOSE).id(purpose);

        if (domain != null) {
            builder.set(DataIntegrityVocab.DOMAIN).string(domain);
        }
        if (challenge != null) {
            builder.set(DataIntegrityVocab.CHALLENGE).string(challenge);
        }
        if (nonce != null) {
            builder.set(DataIntegrityVocab.NONCE).string(nonce);
        }

        final DataIntegrityProof<S> proof = new DataIntegrityProof<>(this, crypto, builder.build());
        proof.created = created;
        proof.purpose = purpose;
        proof.method = method;
        proof.domain = domain;
        proof.challenge = challenge;
        proof.nonce = nonce;
        return proof;
    }
}
