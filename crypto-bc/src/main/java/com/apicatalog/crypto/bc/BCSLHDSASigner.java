package com.apicatalog.crypto.bc;

import java.security.SecureRandom;
import java.security.SignatureException;

import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.SLHDSAParameters;
import org.bouncycastle.crypto.params.SLHDSAPrivateKeyParameters;
import org.bouncycastle.crypto.signers.SLHDSASigner;

/**
 * Signs data using the Bouncy Castle SLH-DSA implementation.
 * <p>
 * This signer supports the {@code sha2_128s} parameter set and can optionally
 * use a {@link SecureRandom} source during signature generation.
 * </p>
 */
public final class BCSLHDSASigner {

    private final SLHDSAPrivateKeyParameters privateKeyParams;
    private SecureRandom random;

    /**
     * Creates a signer using the provided private key parameters and optional
     * source of randomness.
     *
     * @param privateKeyParams the SLH-DSA private key parameters
     * @param random           the source of randomness, or {@code null} to use
     *                         deterministic signing
     */
    public BCSLHDSASigner(SLHDSAPrivateKeyParameters privateKeyParams, SecureRandom random) {
        this.privateKeyParams = privateKeyParams;
        this.random = random;
    }

    /**
     * Creates a signer for the SLH-DSA {@code sha2_128s} parameter set.
     *
     * @param privateKey the encoded private key
     * @return a new signer instance
     */
    public static BCSLHDSASigner new128sInstance(byte[] privateKey) {
        return new128sInstance(privateKey, null);
    }

    /**
     * Creates a signer for the SLH-DSA {@code sha2_128s} parameter set using the
     * specified source of randomness.
     *
     * @param privateKey the encoded private key
     * @param random     the source of randomness, or {@code null}
     * @return a new signer instance
     */
    public static BCSLHDSASigner new128sInstance(byte[] privateKey, SecureRandom random) {
        return new BCSLHDSASigner(toPrivateKeyParams(SLHDSAParameters.sha2_128s, privateKey), random);
    }

    /**
     * Generates an SLH-DSA signature for the supplied data.
     *
     * @param data the data to sign
     * @return the generated signature
     * @throws SignatureException if an error occurs during signature generation
     */
    public byte[] sign(final byte[] data) throws SignatureException {
        var signer = new SLHDSASigner();

        if (random != null) {
            signer.init(true, new ParametersWithRandom(privateKeyParams, random));
        } else {
            signer.init(true, privateKeyParams);
        }

        return signer.generateSignature(data);
    }

    /**
     * Sets the source of randomness used for subsequent signature generation.
     *
     * @param random the source of randomness, or {@code null}
     * @return this signer instance
     */
    public BCSLHDSASigner random(SecureRandom random) {
        this.random = random;
        return this;
    }

    private static SLHDSAPrivateKeyParameters toPrivateKeyParams(SLHDSAParameters params, final byte[] privKey) {
        return new SLHDSAPrivateKeyParameters(params, privKey);
    }
}