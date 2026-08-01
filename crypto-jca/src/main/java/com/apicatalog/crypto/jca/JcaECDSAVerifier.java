package com.apicatalog.crypto.jca;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

/**
 * A verifier implementation for ECDSA signatures using the Java Cryptography
 * Architecture (JCA).
 */
public final class JcaECDSAVerifier {

    private static final JcaECDSAVerifier P256_INSTANCE = new JcaECDSAVerifier(
            "SHA256withECDSA",
            JcaECDSAVerifier::toP256PublicKey);

    private static final JcaECDSAVerifier P384_INSTANCE = new JcaECDSAVerifier(
            "SHA384withECDSA",
            JcaECDSAVerifier::toP384PublicKey);

    /**
     * Functional interface for converting a raw public key into a PublicKey
     * instance.
     */
    @FunctionalInterface
    public interface PublicKeyAdapter {
        /**
         * Converts the raw byte array of a public key into a JCA PublicKey.
         *
         * @param keyFactory   the KeyFactory to use for generation
         * @param rawPublicKey the raw public key bytes
         * @return the generated PublicKey
         * @throws InvalidKeyException if the raw key is invalid
         */
        PublicKey toPublicKey(KeyFactory keyFactory, byte[] rawPublicKey) throws InvalidKeyException;
    }

    private final String algorithm;
    private final PublicKeyAdapter keyAdapter;

    private JcaECDSAVerifier(String algorithm, PublicKeyAdapter keyAdapter) {
        this.algorithm = algorithm;
        this.keyAdapter = keyAdapter;
    }

    /**
     * Retrieves the singleton instance of the P-256 (SHA256withECDSA) verifier.
     *
     * @return the P-256 verifier instance
     */
    public static JcaECDSAVerifier getP256Instance() {
        return P256_INSTANCE;
    }

    /**
     * Retrieves the singleton instance of the P-384 (SHA384withECDSA) verifier.
     *
     * @return the P-384 verifier instance
     */
    public static JcaECDSAVerifier getP384Instance() {
        return P384_INSTANCE;
    }

    /**
     * Verifies an ECDSA signature for the given data using the provided raw public
     * key.
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

            var publicKey = keyAdapter.toPublicKey(KeyFactory.getInstance("EC"), rawPublicKey);

            var verifier = Signature.getInstance(algorithm);
            verifier.initVerify(publicKey);
            verifier.update(data);

            return verifier.verify(decodeECSignature(signature));

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] decodeECSignature(byte[] signature) {
        // Determine encoding using structural ASN.1 length validation rather than the
        // first byte alone
        if (!isDerEncoded(signature) && (signature.length == 64 || signature.length == 96)) {
            return rawToDerSignature(signature);
        }
        return signature;
    }

    private static boolean isDerEncoded(byte[] signature) {
        // Minimum length for a valid DER ECDSA signature is 8 bytes:
        // 0x30 (1) + total_len (1) + 0x02 (1) + r_len (1) + r_val (1) + 0x02 (1) +
        // s_len (1) + s_val (1)
        // Maximum length for standard curves like secp256k1/prime256v1 is 72 bytes.
        if (signature == null || signature.length < 8 || signature.length > 72) {
            return false;
        }

        // Sequence tag verification
        if ((signature[0] & 0xFF) != 0x30) {
            return false;
        }

        // Total length verification (must match array length minus header)
        if ((signature[1] & 0xFF) != signature.length - 2) {
            return false;
        }

        // Target indices for R element
        int rTagIndex = 2;
        int rLenIndex = 3;
        int rValueIndex = 4;

        if ((signature[rTagIndex] & 0xFF) != 0x02) {
            return false;
        }

        int rLen = signature[rLenIndex] & 0xFF;
        if (rLen == 0 || rValueIndex + rLen >= signature.length) {
            return false;
        }

        // R value validation: Cannot be negative
        if ((signature[rValueIndex] & 0x80) != 0) {
            return false;
        }

        // R value validation: No redundant leading zeros allowed
        if (rLen > 1 && signature[rValueIndex] == 0x00 && (signature[rValueIndex + 1] & 0x80) == 0) {
            return false;
        }

        // Target indices for S element
        int sTagIndex = rValueIndex + rLen;
        int sLenIndex = sTagIndex + 1;
        int sValueIndex = sTagIndex + 2;

        // Ensure S header bounds match total length exactly
        if (sValueIndex >= signature.length) {
            return false;
        }

        if ((signature[sTagIndex] & 0xFF) != 0x02) {
            return false;
        }

        int sLen = signature[sLenIndex] & 0xFF;
        if (sLen == 0 || sValueIndex + sLen != signature.length) {
            return false;
        }

        // S value validation: Cannot be negative
        if ((signature[sValueIndex] & 0x80) != 0) {
            return false;
        }

        // S value validation: No redundant leading zeros allowed
        if (sLen > 1 && signature[sValueIndex] == 0x00 && (signature[sValueIndex + 1] & 0x80) == 0) {
            return false;
        }

        return true;
    }

    private static byte[] rawToDerSignature(byte[] rawSignature) {
        if (rawSignature == null || rawSignature.length == 0 || rawSignature.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid raw signature length");
        }

        int coordinateLength = rawSignature.length / 2;

        byte[] rBytes = java.util.Arrays.copyOfRange(rawSignature, 0, coordinateLength);
        byte[] sBytes = java.util.Arrays.copyOfRange(rawSignature, coordinateLength, rawSignature.length);

        java.math.BigInteger r = new java.math.BigInteger(1, rBytes);
        java.math.BigInteger s = new java.math.BigInteger(1, sBytes);

        byte[] rDer = r.toByteArray();
        byte[] sDer = s.toByteArray();

        int rLenFieldSize = (rDer.length < 128) ? 1 : 1 + countLengthBytes(rDer.length);
        int sLenFieldSize = (sDer.length < 128) ? 1 : 1 + countLengthBytes(sDer.length);

        // Total content inside the sequence: Tag(1) + LengthField + Value for both R
        // and S
        int contentLength = 1 + rLenFieldSize + rDer.length + 1 + sLenFieldSize + sDer.length;

        int seqLenFieldSize = (contentLength < 128) ? 1 : 1 + countLengthBytes(contentLength);
        int totalLength = 1 + seqLenFieldSize + contentLength;

        byte[] derOutput = new byte[totalLength];
        int offset = 0;

        // Write Sequence Tag
        derOutput[offset++] = 0x30;
        offset = writeDerLengthToBuffer(derOutput, offset, contentLength);

        // Write R Integer Tag
        derOutput[offset++] = 0x02;
        offset = writeDerLengthToBuffer(derOutput, offset, rDer.length);
        System.arraycopy(rDer, 0, derOutput, offset, rDer.length);
        offset += rDer.length;

        // Write S Integer Tag
        derOutput[offset++] = 0x02;
        offset = writeDerLengthToBuffer(derOutput, offset, sDer.length);
        System.arraycopy(sDer, 0, derOutput, offset, sDer.length);

        return derOutput;
    }

    private static int countLengthBytes(int length) {
        int bytes = 0;
        while (length > 0) {
            bytes++;
            length >>= 8;
        }
        return bytes;
    }

    private static int writeDerLengthToBuffer(byte[] buffer, int offset, int length) {
        if (length < 128) {
            buffer[offset++] = (byte) length;
        } else {
            int numBytes = countLengthBytes(length);
            buffer[offset++] = (byte) (0x80 | numBytes);
            for (int i = numBytes - 1; i >= 0; i--) {
                buffer[offset++] = (byte) ((length >> (8 * i)) & 0xFF);
            }
        }
        return offset;
    }

    private static PublicKey toP256PublicKey(KeyFactory keyFactory, byte[] compressed) throws InvalidKeyException {
        return toECPublicKey("secp256r1", keyFactory, compressed);
    }

    private static PublicKey toP384PublicKey(KeyFactory keyFactory, byte[] compressed) throws InvalidKeyException {
        return toECPublicKey("secp384r1", keyFactory, compressed);
    }

    private static PublicKey toECPublicKey(String curveName, KeyFactory keyFactory, byte[] compressed)
            throws InvalidKeyException {
        try {
            var params = AlgorithmParameters.getInstance("EC");
            params.init(new ECGenParameterSpec(curveName));
            ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

            byte[] xBytes = new byte[compressed.length - 1];
            System.arraycopy(compressed, 1, xBytes, 0, xBytes.length);
            BigInteger x = new BigInteger(1, xBytes);

            BigInteger y = decompressNistY(x, compressed[0], ecSpec.getCurve());

            ECPoint point = new ECPoint(x, y);
            ECPublicKeySpec spec = new ECPublicKeySpec(point, ecSpec);
            return keyFactory.generatePublic(spec);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);

        } catch (InvalidParameterSpecException | InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static BigInteger decompressNistY(BigInteger x, byte prefix, EllipticCurve curve) {
        BigInteger a = curve.getA();
        BigInteger b = curve.getB();
        BigInteger p = ((java.security.spec.ECFieldFp) curve.getField()).getP();

        // y^2 = x^3 + ax + b
        BigInteger rhs = x.multiply(x).multiply(x).add(a.multiply(x)).add(b).mod(p);
        BigInteger y = rhs.modPow(p.add(BigInteger.ONE).shiftRight(2), p);

        if (y.testBit(0) != (prefix == 0x03)) {
            y = p.subtract(y);
        }
        return y;
    }
}
