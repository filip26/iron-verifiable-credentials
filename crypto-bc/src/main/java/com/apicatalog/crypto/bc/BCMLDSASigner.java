package com.apicatalog.crypto.bc;

import java.security.SecureRandom;
import java.security.SignatureException;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.MLDSAParameters;
import org.bouncycastle.crypto.params.MLDSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.MLDSASigner;

/**
 * A signer for ML-DSA (Module-Lattice-Based Digital Signature Algorithm)
 * signatures utilizing the BouncyCastle provider.
 */
public final class BCMLDSASigner {

    private final MLDSAPrivateKeyParameters privateKeyParams;
    private SecureRandom random;

    /**
     * Constructs a new BCMLDSASigner instance.
     *
     * @param privateKeyParams the ML-DSA private key parameters
     * @param random           the source of randomness
     */
    public BCMLDSASigner(MLDSAPrivateKeyParameters privateKeyParams, SecureRandom random) {
        this.privateKeyParams = privateKeyParams;
        this.random = random;
    }

    /**
     * Creates a new deterministic ML-DSA-44 signer instance.
     *
     * @param privateKey the raw bytes of the private key
     * @return a BCMLDSASigner instance for ML-DSA-44
     */
    public static BCMLDSASigner new44Instance(byte[] privateKey) {
        return new44Instance(privateKey, null);
    }

    /**
     * Creates a new ML-DSA-44 signer instance with a specific secure random source.
     *
     * @param privateKey the raw bytes of the private key
     * @param randon     the source of randomness
     * @return a BCMLDSASigner instance for ML-DSA-44
     */
    public static BCMLDSASigner new44Instance(byte[] privateKey, SecureRandom randon) {
        return new BCMLDSASigner(toPrivateKeyParams(privateKey), randon);
    }

    /**
     * Generates an ML-DSA-44 signature for the provided data.
     *
     * @param data the data to be signed
     * @return the raw bytes of the generated signature
     * @throws SignatureException if a signing error occurs
     */
    public byte[] sign(final byte[] data) throws SignatureException {

        try {

            var signer = new MLDSASigner();

            if (random != null) {
                signer.init(true, new ParametersWithRandom(privateKeyParams, random));
            } else {
                signer.init(true, privateKeyParams);
            }

            signer.update(data, 0, data.length);

            return signer.generateSignature();

        } catch (CryptoException e) {
            throw new IllegalStateException("Failed to generate ML-DSA-44 signature", e);
        }
    }

    /**
     * Sets the source of randomness used for subsequent signature generation.
     *
     * @param random the source of randomness, or {@code null}
     * @return this signer instance
     */
    public BCMLDSASigner random(SecureRandom random) {
        this.random = random;
        return this;
    }

    private static MLDSAPrivateKeyParameters toPrivateKeyParams(final byte[] privKey) {
        return new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_44, privKey);
    }
}