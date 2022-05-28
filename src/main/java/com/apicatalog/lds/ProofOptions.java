package com.apicatalog.lds;

import java.time.Instant;

import com.apicatalog.vc.proof.VerificationMethod;


public interface ProofOptions {
    
    VerificationMethod getVerificationMethod();
    
    Instant getCreated();
    
    String getdomain();
    
//    String publicKey;

    
//    DocumentLoader loader;
//    
//    public ProofOptions(DocumentLoader loader) {
//        this.loader = loader;
//    }
//    
//    public VerificationMethod getVerificationMethod() {
//        return new VerificationKeyReference(URI.create("https://github.com/filip26/iron-verifiable-credentials/issue/0001-keys.json"), loader);
//    }
//    
//    public String getCreated() {
//        return "2022-12-11T03:50:55Z";
//    }    
}
