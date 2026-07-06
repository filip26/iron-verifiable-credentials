package com.apicatalog.crypto.bc;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

public final class BCECDSAVerifier {

    private static BCECDSAVerifier P256_VERIFIER = new BCECDSAVerifier(
            "SHA256withECDSA",
            "secp256r1");

    private static BCECDSAVerifier P384_VERIFIER = new BCECDSAVerifier(
            "SHA384withECDSA",
            "secp384r1");

    private final String algorithm;
    private final String curve;

    public BCECDSAVerifier(String algorithm, String curve) {
        this.algorithm = algorithm;
        this.curve = curve;
    }

    public static BCECDSAVerifier getP256Instance() {
        return P256_VERIFIER;
    }

    public static BCECDSAVerifier getP384Instance() {
        return P384_VERIFIER;
    }

    public boolean verify(final byte[] publicKey, final byte[] data, final byte[] signature)
            throws SignatureException, InvalidKeyException {

        try {
            var verifier = Signature.getInstance(algorithm);

            verifier.initVerify(getPublicKeyFromBytes(publicKey));

            verifier.update(data);

            return verifier.verify(toDerSignature(signature));

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);

        } catch (InvalidKeySpecException e) {
            throw new InvalidKeyException(e);

        } catch (IOException e) {
            throw new SignatureException(e);
        }
    }

    private PublicKey getPublicKeyFromBytes(final byte[] pubKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        var keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());

        var spec = ECNamedCurveTable.getParameterSpec(curve);
        var params = new ECNamedCurveSpec(curve, spec.getCurve(), spec.getG(), spec.getN(), spec.getH());
        var point = ECPointUtil.decodePoint(params.getCurve(), pubKey);
        var pubKeySpec = new ECPublicKeySpec(point, params);

        return (ECPublicKey) keyFactory.generatePublic(pubKeySpec);
    }

    private static byte[] toDerSignature(final byte[] signature) throws IOException {

        if (signature == null) {
            throw new IllegalArgumentException("'signature' parameter must not be null.");
        }
        if (signature.length != 64 && signature.length != 96) {
            throw new IllegalArgumentException("'signature' must be exactly 64 or 96 bytes long.");
        }

        final byte[] rBytes = Arrays.copyOfRange(signature, 0, signature.length / 2);
        final byte[] sBytes = Arrays.copyOfRange(signature, signature.length / 2, signature.length);

        final BigInteger r = new BigInteger(1, rBytes);
        final BigInteger s = new BigInteger(1, sBytes);

        final DERSequence sequence = new DERSequence(new ASN1Encodable[] {
                new ASN1Integer(r),
                new ASN1Integer(s)
        });

        return sequence.getEncoded();
    }

}