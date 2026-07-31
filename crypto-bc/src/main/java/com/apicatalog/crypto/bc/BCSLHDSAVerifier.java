package com.apicatalog.crypto.bc;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import org.bouncycastle.crypto.params.SLHDSAParameters;
import org.bouncycastle.crypto.params.SLHDSAPublicKeyParameters;
import org.bouncycastle.crypto.signers.SLHDSASigner;

/**
 * Verifies SLH-DSA signatures using the Bouncy Castle implementation.
 * <p>
 * This verifier is configured for the {@code sha2_128s} parameter set.
 * </p>
 */
public final class BCSLHDSAVerifier {

    private static final BCSLHDSAVerifier INSTANCE_128S = new BCSLHDSAVerifier();

    private BCSLHDSAVerifier() {
        // protected, reserved
    }

    /**
     * Returns the singleton verifier instance for the SLH-DSA {@code sha2_128s}
     * parameter set.
     *
     * @return the singleton verifier instance
     */
    public static BCSLHDSAVerifier get128sInstance() {
        return INSTANCE_128S;
    }

    /**
     * Verifies an SLH-DSA signature.
     *
     * @param publicKey the encoded public key
     * @param data      the signed data
     * @param signature the signature to verify
     * @return {@code true} if the signature is valid; {@code false} otherwise
     * @throws SignatureException  if an error occurs during signature verification
     * @throws InvalidKeyException if the supplied public key is invalid
     */
    public boolean verify(final byte[] publicKey, final byte[] data, final byte[] signature)
            throws SignatureException, InvalidKeyException {

        var verifier = new SLHDSASigner();

        verifier.init(false, getPublicKeyFromBytes(publicKey));

        return verifier.verifySignature(data, signature);

    }

    private static SLHDSAPublicKeyParameters getPublicKeyFromBytes(final byte[] publicKey) {
        return new SLHDSAPublicKeyParameters(SLHDSAParameters.sha2_128s, publicKey);
    }
}