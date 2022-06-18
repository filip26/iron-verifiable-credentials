package com.apicatalog.ld.signature.ed25519;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.ld.signature.proof.VerificationMethod;

public class Ed25519ProofOptions2020 implements ProofOptions {

    VerificationMethod verificationMethod;
    Instant created;
    String domain;
    URI purpose;

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
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setVerificationMethod(VerificationMethod verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    @Override
    public URI getPurpose() {
        return purpose;
    }

    public void setPurpose(URI purpose) {
        this.purpose = purpose;
    }

    @Override
    public String getType() {
        return "https://w3id.org/security#Ed25519Signature2020";
    }
}
