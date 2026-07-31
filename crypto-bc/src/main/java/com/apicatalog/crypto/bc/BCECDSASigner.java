package com.apicatalog.crypto.bc;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Supplier;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.crypto.signers.RandomDSAKCalculator;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.util.BigIntegers;

/**
 * A signer for ECDSA signatures utilizing the BouncyCastle provider. Supports
 * P-256 (secp256r1) and P-384 (secp384r1) curves with optional random or
 * deterministic signing.
 */
public final class BCECDSASigner {

    private final ECPrivateKeyParameters privateKeyParams;
    private final Supplier<ExtendedDigest> digestorFactory;

    private SecureRandom random;

    /**
     * Constructs a new BCECDSASigner instance.
     *
     * @param privateKeyParams the private key parameters
     * @param digestorFactory  the supplier for the extended digest
     * @param random           the source of randomness, or null for deterministic
     *                         signing
     */
    public BCECDSASigner(
            ECPrivateKeyParameters privateKeyParams,
            Supplier<ExtendedDigest> digestorFactory,
            SecureRandom random) {
        this.privateKeyParams = privateKeyParams;
        this.digestorFactory = digestorFactory;
        this.random = random;
    }

    /**
     * Creates a new deterministic signer instance configured for the P-256 curve
     * (secp256r1).
     *
     * @param privateKey the raw bytes of the private key
     * @return a BCECDSASigner instance for P-256
     * @throws InvalidKeySpecException if the provided private key is invalid
     */
    public static BCECDSASigner newP256Instance(byte[] privateKey) throws InvalidKeySpecException {
        return newP256Instance(privateKey, null);
    }

    /**
     * Creates a new signer instance configured for the P-256 curve (secp256r1) with
     * a specific secure random source.
     *
     * @param privateKey the raw bytes of the private key
     * @param random     the source of randomness
     * @return a BCECDSASigner instance for P-256
     * @throws InvalidKeySpecException if the provided private key is invalid
     */
    public static BCECDSASigner newP256Instance(byte[] privateKey, SecureRandom random) throws InvalidKeySpecException {
        return new BCECDSASigner(
                BCECDSASigner.getPrivateKeyFromBytes("secp256r1", privateKey),
                SHA256Digest::new,
                random);
    }

    /**
     * Creates a new deterministic signer instance configured for the P-384 curve
     * (secp384r1).
     *
     * @param privateKey the raw bytes of the private key
     * @return a BCECDSASigner instance for P-384
     * @throws InvalidKeySpecException if the provided private key is invalid
     */
    public static BCECDSASigner newP384Instance(byte[] privateKey) throws InvalidKeySpecException {
        return newP384Instance(privateKey, null);
    }

    /**
     * Creates a new signer instance configured for the P-384 curve (secp384r1) with
     * a specific secure random source.
     *
     * @param privateKey the raw bytes of the private key
     * @param random     the source of randomness
     * @return a BCECDSASigner instance for P-384
     * @throws InvalidKeySpecException if the provided private key is invalid
     */
    public static BCECDSASigner newP384Instance(byte[] privateKey, SecureRandom random) throws InvalidKeySpecException {
        return new BCECDSASigner(
                BCECDSASigner.getPrivateKeyFromBytes("secp384r1", privateKey),
                SHA384Digest::new,
                random);
    }

    /**
     * Generates an ECDSA signature for the provided data.
     *
     * @param data the data to be signed
     * @return the raw bytes of the generated signature
     */
    public byte[] sign(final byte[] data) {

        final ExtendedDigest digest = digestorFactory.get();

        var hash = new byte[digest.getDigestSize()];
        digest.update(data, 0, data.length);
        digest.doFinal(hash, 0);

        var signer = new ECDSASigner((random == null)
                ? new HMacDSAKCalculator(digest)
                : new RandomDSAKCalculator());

        if (random != null) {
            signer.init(true, new ParametersWithRandom(privateKeyParams, random));
        } else {
            signer.init(true, privateKeyParams);
        }

        return toByteArray(signer.generateSignature(hash));
    }

    /**
     * Sets the source of randomness used for subsequent signature generation.
     *
     * @param random the source of randomness, or {@code null}
     * @return this signer instance
     */
    public BCECDSASigner random(SecureRandom random) {
        this.random = random;
        return this;
    }

    private static byte[] toByteArray(BigInteger[] signature) {
        var r = BigIntegers.asUnsignedByteArray(signature[0]);
        var s = BigIntegers.asUnsignedByteArray(signature[1]);

        var bytes = new byte[r.length + s.length];

        System.arraycopy(r, 0, bytes, 0, r.length);
        System.arraycopy(s, 0, bytes, r.length, s.length);

        return bytes;
    }

    private static ECPrivateKeyParameters getPrivateKeyFromBytes(final String curve, final byte[] privKey)
            throws InvalidKeySpecException {

        var spec = ECNamedCurveTable.getParameterSpec(curve);
        var ecParams = new ECDomainParameters(
                spec.getCurve(),
                spec.getG(),
                spec.getN(),
                spec.getH());

        return new ECPrivateKeyParameters(new BigInteger(1, privKey), ecParams);
    }
}