package com.apicatalog.vc.di;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vcdm.VcdmVersion;

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

    public DataIntegrityProofDraft(
            DataIntegritySuite suite,
            VerificationMethod method,
            URI purpose) {
        super(method);
        this.suite = suite;
        this.purpose = purpose;
    }

    public DataIntegrityProofDraft(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            URI method,
            URI purpose) {
        super(method);
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
    public Collection<String> context(VcdmVersion model) {
        if (VcdmVersion.V11.equals(model)) {
            return V1_CONTEXTS;
        }
        return V2_CONTEXTS;
    }

    @Override
    public JsonObject unsigned() {
//FIXME        return unsigned(new LdNodeBuilder()).build();
        return null;
    }
    
    /**
     * Returns an expanded signed proof. i.e. the given proof with proof value attached.
     * 
     * @param unsignedProof
     * @param proofValue
     * @return
     */
    public static final JsonObject signed(JsonObject unsignedProof, JsonObject proofValue) {
//FIXME        return LdNodeBuilder.of(unsignedProof).set(VcdiVocab.PROOF_VALUE).value(proofValue).build();
        return null;
    }
    
//    protected LdNodeBuilder unsigned(LdNodeBuilder builder) {
//
//        super.unsigned(builder, suite.methodAdapter);
//        
//        builder.type(VcdiVocab.TYPE.uri());
//        builder.set(VcdiVocab.CRYPTO_SUITE).scalar("https://w3id.org/security#cryptosuiteString", suite.cryptosuiteName);
//        
//        builder.set(VcdiVocab.PURPOSE).id(purpose);
//        
//        builder.set(VcdiVocab.CREATED).xsdDateTime(created != null ? created : Instant.now());
//
//        if (domain != null) {
//            builder.set(VcdiVocab.DOMAIN).string(domain);
//        }
//        if (challenge != null) {
//            builder.set(VcdiVocab.CHALLENGE).string(challenge);
//        }
//        if (nonce != null) {
//            builder.set(VcdiVocab.NONCE).string(nonce);
//        }
//
//        return builder;
//    }
}
