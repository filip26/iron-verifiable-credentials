package com.apicatalog.di.signature;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.processor.PayloadSelector;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class ProofValue implements Signature {

    private final String algorithm;
    private final String digestAlgorithm;

    private final byte[] value;

    private final DigestiblePayload payload;
    private final Proof proof;

    private ProofValue(
            String algorithm,
            String digestAlgorithm,
            byte[] value,
            Proof proof,
            DigestiblePayload payload) {
        this.algorithm = algorithm;
        this.digestAlgorithm = digestAlgorithm;
        this.value = value;
        this.payload = payload;
        this.proof = proof;
    }

    public static ProofValue newInstance(
            String algorithm,
            String digestAlgorithm,
            byte[] value,
            Proof proof,
            PayloadSelector payload) {
        return new ProofValue(
                algorithm,
                digestAlgorithm,
                value,
                proof,
                payload.digestible());
    }

    public static ProofValue generateSignature(
            String algorithm,
            AsymmetricSigner signer,
            MessageDigest messageDigest,
            Proof proof,
            DigestiblePayload payload) throws SignatureException {

        var digest = digest(messageDigest, proof.canonicalPayload(), payload);

        return new ProofValue(
                algorithm,
                messageDigest.getAlgorithm(),
                signer.sign(digest),
                proof,
                payload);
    }

    @Override
    public boolean verify(
            AsymmetricVerifier verifier,
            Function<String, MessageDigest> digestFactory,
            byte[] publicKey)
            throws InvalidKeyException, SignatureException {

        var digestor = digestFactory.apply(digestAlgorithm);

        var digest = digest(digestor, proof.canonicalPayload(), payload);

        return verifier.verify(publicKey, digest, toByteArray());
    }

    /**
     * Computes the cryptographic hash of the canonical proof concatenated with the
     * cryptographic hash of the canonical document.
     * <p>
     * The output is structured as H(canonicalProof) || H(canonicalDocument) and is
     * utilized as signing or verification data.
     * 
     * @param digest
     * @param canonicalProof    the byte array representing the canonicalized proof
     * @param canonicalDocument the byte array representing the canonicalized
     *                          document
     * @return a byte array containing the concatenated hashes in the specified
     *         order
     */
    private static byte[] digest(
            MessageDigest digest,
            byte[] canonicalProof,
            DigestiblePayload payload) {

        digest.update(canonicalProof);
        var proofHash = digest.digest();

        var payloadHash = payload.digest(digest.getAlgorithm());
        
        if (payloadHash == null) {
            digest.update(payload.canonicalPayload());
            payloadHash = digest.digest();
            payload.digest(digest.getAlgorithm(), payloadHash);
        }

        return digestFromHash(proofHash, payloadHash);
    }

    /**
     * Generates signing or verification data directly from the pre-computed
     * cryptographic digests of the proof and document.
     * <p>
     * The output is structured as H(canonicalProof) || H(canonicalDocument).
     *
     * @param proofHash the cryptographic hash of the canonical proof
     * @param payloadHash   the cryptographic hash of the canonical document
     * @return the signing or verification data block containing the concatenated
     *         hashes
     * @throws NullPointerException if proofHash or docHash is null
     */
    private static byte[] digestFromHash(byte[] proofHash, byte[] payloadHash) {
        var digest = new byte[proofHash.length + payloadHash.length];
        System.arraycopy(proofHash, 0, digest, 0, proofHash.length);
        System.arraycopy(payloadHash, 0, digest, proofHash.length, payloadHash.length);
        return digest;
    }

    @Override
    public byte[] toByteArray() {
        return value;
    }

    @Override
    public Proof proof() {
        return proof;
    }

    @Override
    public String algorithm() {
        return algorithm;
    }

    @Override
    public DigestiblePayload payload() {
        return payload;
    }
}
