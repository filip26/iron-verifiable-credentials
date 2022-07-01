package com.apicatalog.ld.signature.proof;

import java.net.URI;
import java.time.Instant;

public class ProofOptions {

    protected final String type;

    protected final VerificationMethod verificationMethod;

    protected final URI purpose;

    protected Instant ceated;

    protected String domain;

    protected ProofOptions(
	    String type,
	    VerificationMethod verificationMethod,
	    URI purpose
	    ) {
	this.type = type;
	this.verificationMethod = verificationMethod;
	this.purpose = purpose;
    }
    
    public static ProofOptions create(String type, VerificationMethod verificationMethod, URI purpose) {
	return new ProofOptions(type, verificationMethod, purpose);
    }
    
    public VerificationMethod verificationMethod() {
	return verificationMethod;
    }
    
    public Instant ceated() {
	return ceated;
    }
    
    public String domain() {
	return domain;
    }
    
    public String type() {
	return type;
    }
    
    public URI purpose() {
	return purpose;
    }
    
    public ProofOptions created(Instant ceated) {
	this.ceated = ceated;
	return this;
    }
    
    public ProofOptions domain(String domain) {
	this.domain = domain;
	return this;
    }
    
    public Proof toUnsignedProof() {
	final Proof proof = new Proof();
	proof.type = type;
	proof.created = ceated;
	proof.domain = domain;
	proof.purpose = purpose;
	proof.verificationMethod = verificationMethod;
	return proof;
    }
}
