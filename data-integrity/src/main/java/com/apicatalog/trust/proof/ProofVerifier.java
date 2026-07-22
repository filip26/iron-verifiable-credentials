package com.apicatalog.trust.proof;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.MethodResolver;

public class ProofVerifier {

    Collection<String> proofTypes;
    MethodResolver methodResolver;
    Map<String, AsymmetricVerifier> signatureVerifiers;
    Digestor.Factory digestFactory;

    private ProofVerifier(
            Set<String> proofTypes,
            MethodResolver methodResolver,
            Map<String, AsymmetricVerifier> signatureVerifiers,
            Digestor.Factory digestFactory) {
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

        return proof.signature().verify(verifier, digestFactory, publicKey);
    }

    public Collection<Result> verify(ProofCursor cursor, int max) {

        Objects.requireNonNull(cursor);

        if (!cursor.next()) {
            return null;
        }

        int count = 0;

        var results = new ArrayList<Result>();

        do {
            if (count++ == max) {
                throw new IllegalStateException();
            }

            if (!cursor.isAccepted()) {
                results.add(null);
                continue;
            }

            Proof proof = null;
            boolean verified = false;
            Exception error = null;

            try {
                proof = cursor.proof();
                verified = verify(proof);

            } catch (Exception e) {
                error = e;
            }

            results.add(new Result(proof, verified, error));

        } while (cursor.next());

        return results;
    }

    public static record Result(Proof proof, boolean verified, Exception error) {
    };

    public static class Builder {

        Collection<String> proofTypes;
        Map<String, AsymmetricVerifier> verifiers;
        MethodResolver resolver;
        Digestor.Factory digestFactory;

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

        public Builder digestFactory(Digestor.Factory digestFactory) {
            this.digestFactory = digestFactory;
            return this;
        }

        public ProofVerifier build() {
            return new ProofVerifier(Set.copyOf(proofTypes), resolver, Map.copyOf(verifiers), digestFactory);
        }
    }
}
