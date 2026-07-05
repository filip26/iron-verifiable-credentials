package com.apicatalog.trust;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.trust.proof.Proof;

public class ProofVerifier {

    Collection<String> proofTypes;
    MethodResolver methodResolver;
    Map<String, AsymmetricVerifier> signatureVerifiers;

    private ProofVerifier(Set<String> proofTypes, MethodResolver methodResolver,
            Map<String, AsymmetricVerifier> signatureVerifiers) {
        this.proofTypes = proofTypes;
        this.methodResolver = methodResolver;
        this.signatureVerifiers = signatureVerifiers;
    }

    public static Builder newBuilder() {

        return new Builder();
    }

    public boolean verify(Proof proof) throws InvalidKeyException, SignatureException {

        assert (proof != null);

        if (proof.signature() == null) {
            return false;
        }

        if (!proofTypes.contains(proof.type())) {
            throw new IllegalArgumentException();
        }

        var publicKey = methodResolver.resolve(proof);

        return verify(proof, publicKey);
    }

    public boolean verify(Proof proof, byte[] publicKey) throws InvalidKeyException, SignatureException {
        return verify(proof, publicKey, signatureVerifiers.get(proof.signature().algorithm()));
    }

    public static boolean verify(Proof proof, byte[] publicKey, AsymmetricVerifier verifier)
            throws InvalidKeyException, SignatureException {

        assert (proof != null);

        if (proof.signature() == null) {
            return false;
        }

        if (proof.signature() instanceof AtomicSignature atomic) {
            return atomic.verify(verifier, publicKey);
        }

        throw new SignatureException();
    }

    public static class Builder {

        Collection<String> proofTypes;
        Map<String, AsymmetricVerifier> verifiers;
        MethodResolver resolver;

        private Builder() {
            this.proofTypes = new HashSet<String>();
            this.verifiers = new HashMap<>();
        }

        public Builder proof(String proofType) {
            proofTypes.add(proofType);
            return this;
        }

        public Builder resolver(MethodResolver resolver) {
            this.resolver = resolver;
            return this;
        }

        public Builder verifier(String publicKeyAlgorithm, AsymmetricVerifier verifier) {
            verifiers.put(publicKeyAlgorithm, verifier);
            return this;
        }

        public ProofVerifier build() {
            return new ProofVerifier(Set.copyOf(proofTypes), resolver, Map.copyOf(verifiers));
        }
    }
}
