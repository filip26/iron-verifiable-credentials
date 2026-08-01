package com.apicatalog.crypto.jca;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;

/**
 * A verifier implementation for Ed25519 signatures using the Java Cryptography
 * Architecture (JCA).
 */
public final class JcaEd25519Verifier {

    private static final JcaEd25519Verifier INSTANCE = new JcaEd25519Verifier();

    private JcaEd25519Verifier() {
        // protected, reserved
    }

    /**
     * Retrieves the singleton instance of the Ed25519 verifier.
     *
     * @return the Ed25519 verifier instance
     */
    public static JcaEd25519Verifier getInstance() {
        return INSTANCE;
    }

    /**
     * Verifies an Ed25519 signature for the given data using the provided raw
     * public key.
     *
     * @param rawPublicKey the raw byte array of the public key
     * @param data         the original data that was signed
     * @param signature    the signature to verify
     * @return true if the signature is valid, false otherwise
     * @throws InvalidKeyException if the provided key is invalid
     * @throws SignatureException  if an error occurs during signature verification
     */
    public boolean verify(byte[] rawPublicKey, byte[] data, byte[] signature)
            throws InvalidKeyException, SignatureException {

        try {
            var verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(toPublicKey(KeyFactory.getInstance("Ed25519"), rawPublicKey));
            verifier.update(data);

            return verifier.verify(signature);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Loads Ed25519 from 32-byte raw format. Note: Ed25519 raw keys are
     * Little-Endian; Java's EdECPoint expects the standard RFC 8032 representation.
     */
    private static PublicKey toPublicKey(KeyFactory keyFactory, byte[] rawPublicKey) throws InvalidKeyException {
        try {
            // Ed25519 raw keys are essentially the Y-coordinate with a parity bit.
            // We must reverse the array because Java's BigInteger (used internally
            // by some providers) is Big-Endian, while Ed25519 is Little-Endian.
            byte[] reversed = reverse(rawPublicKey.clone());

            // The EdECPoint takes the BigInteger representation of the encoded point
            BigInteger y = new BigInteger(1, reversed);
            EdECPoint point = new EdECPoint(y.testBit(255), y);

            // Construct the spec for Ed25519
            NamedParameterSpec paramSpec = NamedParameterSpec.ED25519;
            EdECPublicKeySpec spec = new EdECPublicKeySpec(paramSpec, point);

            return keyFactory.generatePublic(spec);

        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static byte[] reverse(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
        return array;
    }
}
