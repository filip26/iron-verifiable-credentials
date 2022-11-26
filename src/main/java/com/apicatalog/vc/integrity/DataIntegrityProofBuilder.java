package com.apicatalog.vc.integrity;

import java.time.Instant;

import com.apicatalog.ld.signature.proof.ProofBuilder;

public class DataIntegrityProofBuilder<T extends DataIntegrityProofOptions> implements ProofBuilder<T> {

    T options;

    public <X extends DataIntegrityProofBuilder<?>> X created(Instant created) {
        return (X)this;
    }
    
    public <X extends DataIntegrityProofBuilder<?>> X domain(String domain) {
        options.setDomain(domain);
        return (X)this;
    }
    
    public <X extends DataIntegrityProofBuilder<?>> X challenge(String challenge) {
        options.setChallenge(challenge);
        return (X)this;
    }
    
    @Override
    public T build() {
        return options;
    }
}
