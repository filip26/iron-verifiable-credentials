package com.apicatalog.crypto.jca;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

public final class JcaECDSASigner {

    private String algorithm;
    private PrivateKey privateKey;

    public JcaECDSASigner(String algorithm, PrivateKey privateKey) {
        this.algorithm = algorithm;
        this.privateKey = privateKey;
    }

    public static JcaECDSASigner newP256Instance(byte[] privateKey)
            throws InvalidKeySpecException {
        return new JcaECDSASigner(
                "SHA256withECDSA",
                JcaECDSASigner.toP256PrivateKey(privateKey));

    }

    public static JcaECDSASigner newP384Instance(byte[] privateKey)
            throws InvalidKeySpecException {
        return new JcaECDSASigner(
                "SHA384withECDSA",
                JcaECDSASigner.toP384PrivateKey(privateKey));
    }

    public byte[] sign(byte[] data) throws SignatureException {

        try {
            var signer = Signature.getInstance(algorithm);
            signer.initSign(privateKey);
            signer.update(data);

            return decodeECSignature(signer.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] decodeECSignature(byte[] signature) {
        // Enforce length constraints for P-256 DER signatures (70-72 bytes)
        if (signature != null && signature.length >= 70 && signature.length <= 72 && (signature[0] & 0xFF) == 0x30) {
            return derToRawSignature(signature);
        }
        return signature;
    }

    private static byte[] derToRawSignature(byte[] derSignature) {
        try {
            int offset = 1;

            int totalLenByte = derSignature[offset++] & 0xFF;
            if ((totalLenByte & 0x80) != 0) {
                int numLengthBytes = totalLenByte & 0x7F;
                offset += numLengthBytes;
            }

            if ((derSignature[offset++] & 0xFF) != 0x02) {
                return derSignature;
            }
            int rLen = derSignature[offset++] & 0xFF;
            int rStart = offset;
            offset += rLen;

            if ((derSignature[offset++] & 0xFF) != 0x02) {
                return derSignature;
            }
            int sLen = derSignature[offset++] & 0xFF;
            int sStart = offset;

            byte[] rawOutput = new byte[64];

            // Extract and align R component
            if (rLen > 32) {
                System.arraycopy(derSignature, rStart + (rLen - 32), rawOutput, 0, 32);
            } else {
                System.arraycopy(derSignature, rStart, rawOutput, 32 - rLen, rLen);
            }

            // Extract and align S component
            if (sLen > 32) {
                System.arraycopy(derSignature, sStart + (sLen - 32), rawOutput, 32, 32);
            } else {
                System.arraycopy(derSignature, sStart, rawOutput, 64 - sLen, sLen);
            }

            return rawOutput;
        } catch (Exception e) {
            return derSignature;
        }
    }

    public static PrivateKey toP256PrivateKey(byte[] rawPrivate) throws InvalidKeySpecException {
        return toECPrivateKey("secp256r1", rawPrivate);
    }

    public static PrivateKey toP384PrivateKey(byte[] rawPrivate) throws InvalidKeySpecException {
        return toECPrivateKey("secp384r1", rawPrivate);
    }

    private static PrivateKey toECPrivateKey(String curveName, byte[] rawPrivate)
            throws InvalidKeySpecException {
        try {
            var keyFactory = KeyFactory.getInstance("EC");

            var params = AlgorithmParameters.getInstance("EC");
            params.init(new ECGenParameterSpec(curveName));
            ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

            // Raw private key is a big-endian scalar integer
            BigInteger s = new BigInteger(1, rawPrivate);
            ECPrivateKeySpec spec = new ECPrivateKeySpec(s, ecSpec);

            return keyFactory.generatePrivate(spec);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (InvalidParameterSpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
