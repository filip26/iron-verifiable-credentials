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

public final class BcEcdsaSigner {

    private final ECPrivateKeyParameters privateKeyParams;
    private final Supplier<ExtendedDigest> digestFactory;

    private SecureRandom random;

    public BcEcdsaSigner(
            ECPrivateKeyParameters privateKeyParams,
            Supplier<ExtendedDigest> digestFactory,
            SecureRandom random) {
        this.privateKeyParams = privateKeyParams;
        this.digestFactory = digestFactory;
        this.random = random;
    }

    public static BcEcdsaSigner getP256Instance(byte[] privateKey) throws InvalidKeySpecException {
        return getP256Instance(privateKey, null);
    }

    public static BcEcdsaSigner getP256Instance(byte[] privateKey, SecureRandom random) throws InvalidKeySpecException {
        return new BcEcdsaSigner(
                BcEcdsaSigner.getPrivateKeyFromBytes("secp256r1", privateKey),
                SHA256Digest::new,
                random);
    }

    public static BcEcdsaSigner getP384Instance(byte[] privateKey) throws InvalidKeySpecException {
        return getP384Instance(privateKey, null);
    }

    public static BcEcdsaSigner getP384Instance(byte[] privateKey, SecureRandom random) throws InvalidKeySpecException {
        return new BcEcdsaSigner(
                BcEcdsaSigner.getPrivateKeyFromBytes("secp384r1", privateKey),
                SHA384Digest::new,
                random);
    }

    public byte[] sign(final byte[] data) {

        final ExtendedDigest digest = digestFactory.get();

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

    public BcEcdsaSigner random(SecureRandom random) {
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