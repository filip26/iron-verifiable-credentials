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

public class DataIntegrityProofDraft implements ProofDraft {

    protected static final Collection<String> V1_CONTEXTS = Arrays.asList(
            "https://w3id.org/security/data-integrity/v2",
            "https://w3id.org/security/multikey/v1");

    protected static final Collection<String> V2_CONTEXTS = Arrays.asList(
            "https://www.w3.org/ns/credentials/v2");

    protected final DataIntegritySuite suite;
    protected final CryptoSuite crypto;
    
    protected VerificationMethod method;
    protected URI purpose;
    protected Instant created;
    protected String domain;
    protected String challenge;
    protected String nonce;

    protected JsonObject expanded;

    public DataIntegrityProofDraft(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            VerificationMethod method,
            URI purpose
            ) {
        this.suite = suite;
        this.crypto = crypto;
        this.method = method;
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
        return copy(null);
    }

    @Override
    public JsonObject signedCopy(JsonObject proofValue) {        
        return copy(proofValue);
    }
    
    protected JsonObject copy(JsonObject proofValue) {
        final LdNodeBuilder builder = new LdNodeBuilder();

        builder.type(DataIntegritySuite.PROOF_TYPE_ID);
        builder.set(DataIntegrityVocab.CRYPTO_SUITE).scalar("https://w3id.org/security#cryptosuiteString", suite.cryptosuite);
        builder.set(DataIntegrityVocab.VERIFICATION_METHOD).map(suite.methodAdapter, method);
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
        
        if (proofValue != null) {
            builder.set(DataIntegrityVocab.PROOF_VALUE).value(proofValue);
        }

        return builder.build();
    }
}
