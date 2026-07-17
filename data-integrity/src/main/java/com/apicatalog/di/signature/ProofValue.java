package com.apicatalog.di.signature;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.processor.PayloadProcessor;
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
            PayloadProcessor payload) {
        return new ProofValue(
                algorithm,
                digestAlgorithm,
                value,
                proof,
                payload.digestible());
    }

    public static ProofValue generateSignature(
            String signatureAlgorithm,
            String digestAlgorithm,
            AsymmetricSigner signer,
            Digestor digestor,
            Proof proof,
            DigestiblePayload payload) throws SignatureException {

        var digest = digest(digestAlgorithm, digestor, proof.canonicalPayload(), payload);

        return new ProofValue(
                signatureAlgorithm,
                digestAlgorithm,
                signer.sign(digest),
                proof,
                payload);
    }

    public static Signature generateSignature(
            String signatureAlgorithm,
            String digestAlgorithm,
            AsymmetricSigner signer,
            Digestor.Factory digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload payload) throws SignatureException {

        var digestor = digestFactory.newDigestor(digestAlgorithm);

        return ProofValue.generateSignature(
                signatureAlgorithm,
                digestAlgorithm,
                signer,
                digestor,
                proof,
                payload);
    }

    public static Signature generateSignatureWithSHA256(
            String signatureAlgorithm,
            AsymmetricSigner signer,
            Digestor.Factory digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload data) throws SignatureException {
        return generateSignature(signatureAlgorithm, Digestor.SHA_256, signer, digestFactory, proof, data);
    }

    public static Signature generateSignatureWithSHA384(
            String algorithm,
            AsymmetricSigner signer,
            Digestor.Factory digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload data) throws SignatureException {
        return generateSignature(algorithm, Digestor.SHA_384, signer, digestFactory, proof, data);
    }

    @Override
    public boolean verify(
            AsymmetricVerifier verifier,
            Digestor.Factory digestFactory,
            byte[] publicKey)
            throws InvalidKeyException, SignatureException {

        var digestor = digestFactory.newDigestor(digestAlgorithm);

        var digest = digest(digestAlgorithm, digestor, proof.canonicalPayload(), payload);

        return verifier.verify(publicKey, digest, toByteArray());
    }

    /**
     * Computes the cryptographic hash of the canonical proof concatenated with the
     * cryptographic hash of the canonical document.
     * <p>
     * The output is structured as H(canonicalProof) || H(canonicalDocument) and is
     * utilized as signing or verification data.
     * 
     * @param digestAlgorithm
     * @param digest
     * @param canonicalProof    the byte array representing the canonicalized proof
     * @param canonicalDocument the byte array representing the canonicalized
     *                          document
     * @return a byte array containing the concatenated hashes in the specified
     *         order
     */
    private static byte[] digest(
            String digestAlgorithm,
            Digestor digest,
            byte[] canonicalProof,
            DigestiblePayload payload) {

        var proofHash = digest.digest(canonicalProof);

        var payloadHash = payload.digest(digestAlgorithm);

        if (payloadHash == null) {
            payloadHash = digest.digest(payload.canonicalPayload());
            payload.digest(digestAlgorithm, payloadHash);
        }

        return digestFromHash(proofHash, payloadHash);
    }

    /**
     * Generates signing or verification data directly from the pre-computed
     * cryptographic digests of the proof and document.
     * <p>
     * The output is structured as H(canonicalProof) || H(canonicalDocument).
     *
     * @param proofHash   the cryptographic hash of the canonical proof
     * @param payloadHash the cryptographic hash of the canonical document
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
