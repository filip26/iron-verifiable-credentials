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
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

public abstract class DataIntegritySuite implements SignatureSuite {

    protected static final String PROOF_TYPE_NAME = "DataIntegrityProof";

    protected static final String PROOF_TYPE_ID = VcVocab.SECURITY_VOCAB + PROOF_TYPE_NAME;

    protected final MethodAdapter methodAdapter;

    protected final String cryptosuite;

    protected DataIntegritySuite(final String cryptosuite, final MethodAdapter method) {
        this.cryptosuite = cryptosuite;
        this.methodAdapter = method;
    }

    protected void validateProofValue(byte[] proofValue) throws DocumentError {
        /* implement custom proof value validation */
    }

    @Override
    public boolean isSupported(String proofType, JsonObject expandedProof) {
        return PROOF_TYPE_ID.equals(proofType) && cryptosuite.equals(getCryptoSuiteName(expandedProof));
    }

    static String getCryptoSuiteName(final JsonObject proof) {
        if (proof == null) {
            throw new IllegalArgumentException("expandedProof property must not be null.");
        }

        try {
            return LdNode.of(proof).scalar(DataIntegrityVocab.CRYPTO_SUITE).string();

        } catch (DocumentError e) {

        }
        return null;
    }

    @Override
    public Proof readProof(JsonObject expandedProof) throws DocumentError {
        return DataIntegrityProofReader.read(expandedProof, this);
    }

    protected abstract CryptoSuite getCryptoSuite(String cryptoName) throws DocumentError;

    protected DataIntegrityProof createDraft(
            CryptoSuite crypto,
            VerificationMethod method,
            URI purpose,
            Instant created,
            String domain,
            String challenge) throws DocumentError {

        final LdNodeBuilder builder = new LdNodeBuilder();

        builder.type(PROOF_TYPE_ID);
        builder.set(DataIntegrityVocab.CRYPTO_SUITE).string(cryptosuite);
        builder.set(DataIntegrityVocab.VERIFICATION_METHOD).map(methodAdapter, method);
        builder.set(DataIntegrityVocab.CREATED).xsdDateTime(created != null ? created : Instant.now());
        builder.set(DataIntegrityVocab.PURPOSE).id(purpose);

        if (domain != null) {
            builder.set(DataIntegrityVocab.DOMAIN).string(domain);
        }
        if (challenge != null) {
            builder.set(DataIntegrityVocab.CHALLENGE).string(challenge);
        }

        final DataIntegrityProof proof = new DataIntegrityProof(this, crypto, builder.build());
        proof.created = created;
        proof.purpose = purpose;
        proof.method = method;
        proof.domain = domain;
        proof.challenge = challenge;
        return proof;
    }
}
