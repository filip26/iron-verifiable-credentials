package com.apicatalog.lds.ed25519;

import java.time.Instant;

import com.apicatalog.lds.ProofOptions;
import com.apicatalog.vc.proof.VerificationMethod;

public class Ed25519ProofOptions2020 implements ProofOptions {

    VerificationMethod verificationMethod;
    Instant created;
    String domain;
    
    @Override
    public Instant getCreated() {
        return created;
    }
    
    public void setCreated(Instant created) {
        this.created = created;
    }

    @Override
    public VerificationMethod getVerificationMethod() {
        return verificationMethod;
    }

    @Override
    public String getdomain() {
        return domain;
    }

    public void setVerificationMethod(VerificationMethod verificationMethod) {
        this.verificationMethod = verificationMethod;
    }
    
    @Override
    public String getType() {
        return "https://w3id.org/security#Ed25519Signature2020";
    }
}
