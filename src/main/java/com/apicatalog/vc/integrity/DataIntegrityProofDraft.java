package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.issuer.ProofDraft;

import jakarta.json.JsonObject;

public class DataIntegrityProofDraft extends ProofDraft {

    protected static final Collection<String> V1_CONTEXTS = Arrays.asList(
            "https://w3id.org/security/data-integrity/v2",
            "https://w3id.org/security/multikey/v1");

    protected static final Collection<String> V2_CONTEXTS = Arrays.asList(
            "https://www.w3.org/ns/credentials/v2");

    protected final DataIntegritySuite suite;

    protected final URI purpose;

    protected Instant created;
    protected String domain;
    protected String challenge;
    protected String nonce;

    protected JsonObject expanded;

    public DataIntegrityProofDraft(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            VerificationMethod method,
            URI purpose) {
        super(crypto, method);
        this.suite = suite;
        this.purpose = purpose;
    }

    public DataIntegrityProofDraft(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            URI method,
            URI purpose) {
        super(crypto, method);
        this.suite = suite;
        this.purpose = purpose;
    }

    public void created(Instant created) {
        this.created = created;
    }

    public void challenge(String challenge) {
        this.challenge = challenge;
    }

    public void domain(String domain) {
        this.domain = domain;
    }

    public void nonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public Collection<String> context(ModelVersion model) {
        if (ModelVersion.V11.equals(model)) {
            return V1_CONTEXTS;
        }
        return V2_CONTEXTS;
    }

    @Override
    public JsonObject unsigned() {
        return unsigned(new LdNodeBuilder()).build();
    }
    
    /**
     * Returns an expanded signed proof. i.e. the given proof with proof value attached.
     * 
     * @param unsignedProof
     * @param proofValue
     * @return
     */
    public static final JsonObject signed(JsonObject unsignedProof, JsonObject proofValue) {
        return LdNodeBuilder.of(unsignedProof).set(DataIntegrityVocab.PROOF_VALUE).value(proofValue).build();
    }
    
    protected LdNodeBuilder unsigned(LdNodeBuilder builder) {

        super.unsigned(builder, suite.methodAdapter);
        
        builder.type(DataIntegritySuite.PROOF_TYPE_ID);
        builder.set(DataIntegrityVocab.CRYPTO_SUITE).scalar("https://w3id.org/security#cryptosuiteString", suite.cryptosuite);
        
        builder.set(DataIntegrityVocab.PURPOSE).id(purpose);
        
        builder.set(DataIntegrityVocab.CREATED).xsdDateTime(created != null ? created : Instant.now());

        if (domain != null) {
            builder.set(DataIntegrityVocab.DOMAIN).string(domain);
        }
        if (challenge != null) {
            builder.set(DataIntegrityVocab.CHALLENGE).string(challenge);
        }
        if (nonce != null) {
            builder.set(DataIntegrityVocab.NONCE).string(nonce);
        }

        return builder;
    }
}
