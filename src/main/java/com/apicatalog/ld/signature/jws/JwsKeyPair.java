package com.apicatalog.ld.signature.jws;

import com.nimbusds.jose.jwk.JWK;

import java.net.URI;

public class JwsKeyPair extends JwsVerificationKey {

    protected JWK privateKey;

    public JwsKeyPair() {
        super();
    }

    public JwsKeyPair(URI id) {
        super(id);
    }

    public JWK getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(JWK privateKey) {
        this.privateKey = privateKey;
    }

}
