package com.apicatalog.ld.signature.key;

import java.net.URI;

import com.apicatalog.ld.signature.proof.VerificationMethod;

public class VerificationKey extends VerificationMethod {

    protected byte[] publicKey;

    public VerificationKey() {
    super();
    }

    public VerificationKey(URI id) {
    super(id);
    }

    public byte[] getPublicKey() {
    return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
    this.publicKey = publicKey;
    }
}
