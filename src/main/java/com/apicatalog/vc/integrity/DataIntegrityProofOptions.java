package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.processor.Verifier;

public class DataIntegrityProofOptions extends DataIntegrityProof implements ProofOptions {

    protected final SignatureSuite suite;

    public DataIntegrityProofOptions(final SignatureSuite suite) {
        this.suite = suite;
        this.type = suite.getProofType().id();
    }

    @Override
    public SignatureSuite getSuite() {
        return suite;
    }

    @Override
    public LdObject toUnsignedProof() {

        Map<String, Object> proof = new LinkedHashMap<>();
        
        proof.put(LdTerm.TYPE.id(), type);
        proof.put(DataIntegritySchema.CREATED.id(), created);
        proof.put(DataIntegritySchema.PURPOSE.id(), purpose);
        proof.put(DataIntegritySchema.VERIFICATION_METHOD.id(), method);
        proof.put(DataIntegritySchema.DOMAIN.id(), domain);
        proof.put(DataIntegritySchema.CHALLENGE.id(), challenge);
        
        return new LdObject(proof);
    }

    public DataIntegrityProofOptions verificationMethod(VerificationMethod verificationMethod) {
        this.method = verificationMethod;
        return this;
    }

    public DataIntegrityProofOptions purpose(URI purpose) {
        this.purpose = purpose;
        return this;
    }

    public DataIntegrityProofOptions created(Instant created) {
        this.created = created;
        return this;
    }
    
    public DataIntegrityProofOptions domain(String domain) {
        this.domain = domain;
        return this;
    }
    
    public DataIntegrityProofOptions challenge(String challenge) {
        this.challenge = challenge;
        return this;
    }
    

}
