package com.apicatalog.crypto.gc.kms;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.codec.KeyCodec;

/**
 * Utility class for exporting public keys retrieved from Google Cloud KMS into
 * Multibase-encoded formats required by W3C Data Integrity specifications.
 */
public final class KmsPublicKeyExporter {

    private KmsPublicKeyExporter() {
        // protected, reserved
    }

    /**
     * Converts a Google Cloud KMS public key into its corresponding
     * Multibase-encoded string.
     *
     * @param publicKey the Google Cloud KMS public key object, must not be null
     * @return the Multibase-encoded public key string
     * @throws IllegalArgumentException if the key type or algorithm is unsupported
     */
    public static String publicKeyMultibase(com.google.cloud.kms.v1.PublicKey publicKey) {

        Objects.requireNonNull(publicKey, "publicKey must not be null");

        return switch (publicKey.getAlgorithm()) {
        case EC_SIGN_P256_SHA256 -> Multibase.BASE_58_BTC.encode(
                KeyCodec.P256_PUBLIC.encode(
                        KmsPublicKeyExporter.exportRawECKey(publicKey)));

        case EC_SIGN_P384_SHA384 -> Multibase.BASE_58_BTC.encode(
                KeyCodec.P384_PUBLIC.encode(
                        KmsPublicKeyExporter.exportRawECKey(publicKey)));

        case EC_SIGN_ED25519 -> Multibase.BASE_58_BTC.encode(
                KeyCodec.ED25519_PUBLIC.encode(
                        KmsPublicKeyExporter.exportRawEDKey(publicKey)));

        // PQC Cases: These return raw bytes directly from the KMS response
        case PQ_SIGN_SLH_DSA_SHA2_128S -> Multibase.BASE_64_URL.encode(
                KeyCodec.SLHDSA_SHA2_128S_PUBLIC.encode(
                        publicKey.getPublicKey().getData().toByteArray()));

        case PQ_SIGN_ML_DSA_44 -> Multibase.BASE_64_URL.encode(
                KeyCodec.MLDSA_44_PUBLIC.encode(
                        publicKey.getPublicKey().getData().toByteArray()));

        case PQ_SIGN_ML_DSA_87 -> Multibase.BASE_64_URL.encode(
                KeyCodec.MLDSA_87_PUBLIC.encode(
                        publicKey.getPublicKey().getData().toByteArray()));

        default -> throw new IllegalArgumentException("Unsupported key algorithm [" + publicKey.getAlgorithm() + "]");
        };
    }

    private static byte[] exportRawEDKey(com.google.cloud.kms.v1.PublicKey publicKey) {

        // 1. Get Public Key from KMS (Returns X.509 PEM)
        String pem = publicKey.getPem();
        byte[] derEncoded = KmsPublicKeyExporter.decodePem(pem);

        // 2. Try to parse as EdDSA (Ed25519)
        try {
            KeyFactory edkf = KeyFactory.getInstance("EdDSA");
            PublicKey pubKey = edkf.generatePublic(new X509EncodedKeySpec(derEncoded));
            if (pubKey instanceof EdECPublicKey edKey) {
                return extractEd25519Bytes(edKey);
            }

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EdDSA algorithm not available", e);

        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Unsupported public key specification", e);
        }

        throw new IllegalArgumentException("Unsupported public key type [" + publicKey + "]");
    }

    /**
     * Exports raw compressed bytes from an EC public key retrieved from KMS.
     *
     * @param publicKey the Google Cloud KMS public key object, must not be null
     * @return compressed byte array representation of the NIST EC key
     */
    private static byte[] exportRawECKey(com.google.cloud.kms.v1.PublicKey publicKey) {

        Objects.requireNonNull(publicKey, "publicKey must not be null");

        // 1. Get Public Key from KMS (Returns X.509 PEM)
        String pem = publicKey.getPem();
        byte[] derEncoded = KmsPublicKeyExporter.decodePem(pem);

        // NIST EC (P-256/P-384)
        try {

            KeyFactory eckf = KeyFactory.getInstance("EC");
            ECPublicKey ecKey = (ECPublicKey) eckf.generatePublic(new X509EncodedKeySpec(derEncoded));
            return compressNistKey(ecKey);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EC algorithm not available", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Unsupported public key specification", e);
        }
    }

    private static byte[] extractEd25519Bytes(EdECPublicKey key) {
        // Ed25519 public keys in Java are represented by an EdECPoint.
        // The "Y" coordinate already contains the 255-bit y-value
        // and the MSB parity bit for x, following RFC 8032.
        byte[] raw = key.getPoint().getY().toByteArray();

        // Ensure exactly 32 bytes (BigInteger might add a leading 0x00)
        byte[] fixed = new byte[32];
        int length = Math.min(raw.length, 32);
        System.arraycopy(raw, raw.length - length, fixed, 32 - length, length);

        // Ed25519 is Little-Endian; Java's BigInteger is Big-Endian.
        // Most raw Ed25519 consumers expect Little-Endian.
        reverseArray(fixed);
        return fixed;
    }

    private static byte[] compressNistKey(ECPublicKey pubKey) {
        int fieldSize = (pubKey.getParams().getCurve().getField().getFieldSize() + 7) / 8;
        byte[] x = normalize(pubKey.getW().getAffineX().toByteArray(), fieldSize);
        byte prefix = pubKey.getW().getAffineY().testBit(0) ? (byte) 0x03 : (byte) 0x02;

        byte[] compressed = new byte[1 + fieldSize];
        compressed[0] = prefix;
        System.arraycopy(x, 0, compressed, 1, fieldSize);
        return compressed;
    }

    private static byte[] normalize(byte[] data, int length) {
        byte[] fixed = new byte[length];
        int srcPos = Math.max(0, data.length - length);
        int destPos = Math.max(0, length - data.length);
        System.arraycopy(data, srcPos, fixed, destPos, Math.min(data.length, length));
        return fixed;
    }

    private static void reverseArray(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    private static byte[] decodePem(String pem) {
        String clean = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(clean);
    }
}