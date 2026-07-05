package com.apicatalog.trust;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.trust.proof.Proof;

public class ProofVerifier {

    final Map<String, Map.Entry<MethodResolver, AsymmetricVerifier>> verifiers;

    protected ProofVerifier(Map<String, Map.Entry<MethodResolver, AsymmetricVerifier>> verifiers) {
        this.verifiers = verifiers;
    }
    
    public static Builder newBuilder() {

        return new Builder();
    }

    public boolean verify(Proof proof) throws InvalidKeyException, SignatureException {

        assert(proof != null);
        
        if (proof.signature() == null) {
            return false;
        }
        
        var entry = verifiers.get(proof.type());
        
        var publicKey = entry.getKey().resolve(proof);

        return verify(proof, publicKey, entry.getValue());
    }
    
    public boolean verify(Proof proof, byte[] publicKey) throws InvalidKeyException, SignatureException {
        return verify(proof, publicKey, verifiers.get(proof.signature().algorithm()).getValue());
    }
    
    public static boolean verify(Proof proof, byte[] publicKey, AsymmetricVerifier verifier) throws InvalidKeyException, SignatureException {

        assert(proof != null);
        
        if (proof.signature() == null) {
            return false;
        }

        if (proof.signature() instanceof AtomicSignature atomic) {
            return atomic.verify(verifier, publicKey);
        }
        
        throw new SignatureException();
    }
    
    public static class Builder {
        
        Map<String, Map.Entry<MethodResolver, AsymmetricVerifier>> verifiers;
        
        private Builder() {
            this.verifiers = new HashMap<>();
        }
        
        public Builder proof(String proofType, MethodResolver resolver, AsymmetricVerifier verifier) {
            verifiers.put(proofType, Map.entry(resolver, verifier));
            return this;
        }
        
        public ProofVerifier build() {
            return new ProofVerifier(Map.copyOf(verifiers));
        }
    }
}
