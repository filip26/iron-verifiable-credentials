package com.apicatalog.ld.signature.jws;

//import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.nimbusds.jose.jwk.JWK;

import java.net.URI;

/**
 * Based on {@link com.apicatalog.ld.signature.key.VerificationKey}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JwsVerificationKey extends JwsVerificationMethod {

    protected JWK publicKey;

    public JwsVerificationKey() {
        super();
    }

    public JwsVerificationKey(URI id) {
        super(id);
    }

    public JWK getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(JWK publicKey) {
        this.publicKey = publicKey;
    }

}
