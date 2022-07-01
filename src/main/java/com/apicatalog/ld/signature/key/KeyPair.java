package com.apicatalog.ld.signature.key;

import java.net.URI;

public class KeyPair extends VerificationKey {

    protected byte[] privateKey;
    
    public KeyPair() {
	super();
    }

    public KeyPair(URI id) {
	super(id);
    }

    public byte[] getPrivateKey() {
	return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
	this.privateKey = privateKey;
    }
}
