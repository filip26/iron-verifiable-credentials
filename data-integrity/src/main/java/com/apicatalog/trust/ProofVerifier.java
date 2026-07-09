package com.apicatalog.trust;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.AtomicSignature;

public class ProofVerifier {

    Collection<String> proofTypes;
    MethodResolver methodResolver;
    Map<String, AsymmetricVerifier> signatureVerifiers;
    Function<String, MessageDigest> digestFactory;

    private ProofVerifier(
            Set<String> proofTypes,
            MethodResolver methodResolver,
            Map<String, AsymmetricVerifier> signatureVerifiers,
            Function<String, MessageDigest> digestFactory) {
        this.proofTypes = proofTypes;
        this.methodResolver = methodResolver;
        this.signatureVerifiers = signatureVerifiers;
        this.digestFactory = digestFactory;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public boolean verify(Proof proof) throws InvalidKeyException, SignatureException {

        assert (proof != null);

        if (proof.signature() == null) {
            return false;
        }

//FIXME
//        if (!proofTypes.contains(proof.type())) {
//            throw new IllegalArgumentException();
//        }

        var publicKey = methodResolver.resolve(proof);

        return verify(proof, publicKey);
    }

    public boolean verify(Proof proof, byte[] publicKey) throws InvalidKeyException, SignatureException {
        
        Objects.requireNonNull(proof.signature());
        Objects.requireNonNull(proof.signature().algorithm());
        
        var asymmetricVerifier = signatureVerifiers.get(proof.signature().algorithm());
        
        Objects.requireNonNull(asymmetricVerifier);
        
        return verify(proof, publicKey, asymmetricVerifier);
    }

    public boolean verify(Proof proof, byte[] publicKey, AsymmetricVerifier verifier)
            throws InvalidKeyException, SignatureException {

        Objects.requireNonNull(proof);
        Objects.requireNonNull(publicKey);
        Objects.requireNonNull(verifier, "Asymmetric verifier for " + proof.signature().algorithm() + " is null.");

        if (proof.signature() == null) {
            return false;
        }

        if (proof.signature() instanceof AtomicSignature atomic) {
            return atomic.verify(verifier, digestFactory,  publicKey);
        }

        throw new SignatureException();
    }

    public static class Builder {

        Collection<String> proofTypes;
        Map<String, AsymmetricVerifier> verifiers;
        MethodResolver resolver;
        Function<String, MessageDigest> digestFactory;

        private Builder() {
            this.proofTypes = new HashSet<String>();
            this.verifiers = new HashMap<>();
        }

//TODO
//        public Builder proof(String proofType) {
//            proofTypes.add(proofType);
//            return this;
//        }

        public Builder resolver(MethodResolver resolver) {
            this.resolver = resolver;
            return this;
        }

        public Builder verifier(String publicKeyAlgorithm, AsymmetricVerifier verifier) {
            verifiers.put(publicKeyAlgorithm, verifier);
            return this;
        }
        
        public Builder digestFactory(Function<String, MessageDigest> digestFactory) {
            this.digestFactory = digestFactory;
            return this;
        }

        public ProofVerifier build() {
            return new ProofVerifier(Set.copyOf(proofTypes), resolver, Map.copyOf(verifiers), digestFactory);
        }
    }
}
