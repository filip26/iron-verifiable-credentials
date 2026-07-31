package com.apicatalog.crypto.bc;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.NamedParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * A signer for Ed25519 signatures utilizing the BouncyCastle provider.
 */
public final class BCEd25519Signer {

    private final PrivateKey privateKey;

    /**
     * Constructs a new BCEd25519Signer instance.
     *
     * @param privateKey the Ed25519 private key
     */
    public BCEd25519Signer(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Creates a new Ed25519 signer instance using the given private key bytes.
     *
     * @param privateKey the raw bytes of the private key
     * @return a BCEd25519Signer instance
     * @throws NoSuchAlgorithmException      if the Ed25519 algorithm is not
     *                                       available
     * @throws InvalidKeySpecException       if the private key specification is
     *                                       invalid
     * @throws InvalidParameterSpecException if the parameter specification is
     *                                       invalid
     */
    public static BCEd25519Signer newInstance(byte[] privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {
        return new BCEd25519Signer(getPrivateKey(privateKey));
    }

    /**
     * Generates an Ed25519 signature for the provided data.
     *
     * @param data the data to be signed
     * @return the raw bytes of the generated signature
     * @throws SignatureException if a signing error occurs
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

    private static PrivateKey getPrivateKey(byte[] privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {
        var keyFactory = KeyFactory.getInstance("Ed25519", new BouncyCastleProvider());
        NamedParameterSpec paramSpec = new NamedParameterSpec(keyFactory.getAlgorithm());
        EdECPrivateKeySpec spec = new EdECPrivateKeySpec(paramSpec, privateKey);
        return keyFactory.generatePrivate(spec);
    }
}