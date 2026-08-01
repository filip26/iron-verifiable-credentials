package com.apicatalog.crypto.jca;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;

/**
 * A signer implementation for Ed25519 signatures using the Java Cryptography
 * Architecture (JCA).
 */
public final class JcaEd25519Signer {

    private final PrivateKey privateKey;

    /**
     * Constructs a new JcaEd25519Signer with the provided private key.
     *
     * @param privateKey the private key used for signing
     */
    public JcaEd25519Signer(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Creates a new Ed25519 signer instance from a raw private key.
     *
     * @param privateKey the raw byte array of the private key
     * @return a new instance of JcaEd25519Signer
     * @throws InvalidKeySpecException if the provided key specification is invalid
     */
    public static JcaEd25519Signer newInstance(byte[] privateKey) throws InvalidKeySpecException {
        return new JcaEd25519Signer(toPrivateKey(privateKey));
    }

    /**
     * Signs the given data using the Ed25519 algorithm.
     *
     * @param data the data to be signed
     * @return the generated signature
     * @throws SignatureException if an error occurs during the signing process
     */
    public byte[] sign(byte[] data) throws SignatureException {

        try {
            var signer = Signature.getInstance("Ed25519");
            signer.initSign(privateKey);
            signer.update(data);

            return signer.sign();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Loads Ed25519 from 32-byte raw format.
     * 
     * @throws InvalidKeySpecException
     */
    private static PrivateKey toPrivateKey(byte[] rawPrivateKey) throws InvalidKeySpecException {
        try {
            var keyFactory = KeyFactory.getInstance("Ed25519");

            // Construct the spec for Ed25519 using the raw byte array directly
            NamedParameterSpec paramSpec = NamedParameterSpec.ED25519;
            var spec = new EdECPrivateKeySpec(paramSpec, rawPrivateKey);

            return keyFactory.generatePrivate(spec);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
