package com.apicatalog.di.signature;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.SignatureException;

import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.trust.AtomicSignature;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.data.DigestiblePayload;
import com.apicatalog.trust.proof.Proof;

public final class ProofValue implements AtomicSignature {

    private final String algorithm;

    private byte[] digest;
    private final byte[] value;

    private final Data data;
    private final Proof proof;

    private ProofValue(
            String algorithm,
            byte[] digest,
            byte[] value,
            Proof proof,
            Data data) {
        this.algorithm = algorithm;
        this.digest = digest;
        this.value = value;
        this.data = data;
        this.proof = proof;
    }

    public static ProofValue newSignature(
            String algorithm,
            MessageDigest messageDigest,
            byte[] value,
            Proof proof,
            Data data) {

        var digest = digest(messageDigest, proof.canonicalPayload(), data.digestiblePayload(proof.previous()));

        return new ProofValue(
                algorithm,
                digest,
                value,
                proof,
                data);
    }

    public static ProofValue generateSignature(
            AsymmetricSigner signer,
            String algorithm,
            MessageDigest messageDigest,
            Proof proof,
            Data data) throws SignatureException {

        var digest = digest(messageDigest, proof.canonicalPayload(), data.digestiblePayload(proof.previous()));

        return new ProofValue(
                algorithm,
                digest,
                signer.sign(digest),
                proof,
                data);
    }

    @Override
    public boolean verify(AsymmetricVerifier verifier, byte[] publicKey)
            throws InvalidKeyException, SignatureException {
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
            DigestiblePayload document) {

        digest.update(canonicalProof);
        var proofHash = digest.digest();

        var docHash = document.digest(digest.getAlgorithm());
        if (docHash == null) {
            digest.update(document.canonicalPayload());
            docHash = digest.digest();
            document.digest(digest.getAlgorithm(), docHash);
        }

//        System.out.println("Doc Digest: " + HexFormat.of().formatHex(docHash));
        return digestFromHashes(proofHash, docHash);
    }

    /**
     * Generates signing or verification data directly from the pre-computed
     * cryptographic digests of the proof and document.
     * <p>
     * The output is structured as H(canonicalProof) || H(canonicalDocument).
     *
     * @param proofHash the cryptographic hash of the canonical proof
     * @param docHash   the cryptographic hash of the canonical document
     * @return the signing or verification data block containing the concatenated
     *         hashes
     * @throws NullPointerException if proofHash or docHash is null
     */
    private static byte[] digestFromHashes(byte[] proofHash, byte[] docHash) {
        var digest = new byte[proofHash.length + docHash.length];
        System.arraycopy(proofHash, 0, digest, 0, proofHash.length);
        System.arraycopy(docHash, 0, digest, proofHash.length, docHash.length);
//        System.out.println("Digest: " + HexFormat.of().formatHex(digest));
        return digest;
    }

    @Override
    public byte[] toByteArray() {
        return value;
    }

    @Override
    public Data data() {
        return data;
    }

    @Override
    public Proof proof() {
        return proof;
    }

    @Override
    public String algorithm() {
        return algorithm;
    }
}
