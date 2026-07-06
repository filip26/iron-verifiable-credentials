package com.apicatalog.crypto.jca;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public final class JcaMLDSASigner {
    
    private static final String ALGORITHM = "ML-DSA-44";

    private final PrivateKey privateKey;
    private SecureRandom random;

    public JcaMLDSASigner(
            final PrivateKey privateKey,
            final SecureRandom random) {
        this.privateKey = privateKey;
        this.random = random;
    }

    public static JcaMLDSASigner new44Instance(final byte[] privateKey)
            throws InvalidKeySpecException {
        return new44Instance(privateKey, null);
    }

    public static JcaMLDSASigner new44Instance(
            final byte[] privateKey,
            final SecureRandom random) throws InvalidKeySpecException {

        return new JcaMLDSASigner(
                toPrivateKey(privateKey),
                random);
    }

    public byte[] sign(final byte[] data)
            throws SignatureException {

        try {
            var signature = Signature.getInstance(ALGORITHM);

            if (random != null) {
                signature.initSign(privateKey, random);
            } else {
                signature.initSign(privateKey);
            }

            signature.update(data);

            return signature.sign();

        } catch (InvalidKeyException e) {
            throw new IllegalStateException(
                    "Failed to initialize ML-DSA signer",
                    e);
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Failed to generate ML-DSA-44 signature",
                    e);
        }
    }

    public JcaMLDSASigner random(final SecureRandom random) {
        this.random = random;
        return this;
    }

    // NIST FIPS 204 Standard OIDs
    // ML-DSA-44: 2.16.840.1.101.3.4.3.17
    private static final byte[] OID_ML_DSA_44 = { 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x03,
            0x11 };
    // ML-DSA-65: 2.16.840.1.101.3.4.3.18
    private static final byte[] OID_ML_DSA_65 = { 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x03,
            0x12 };
    // ML-DSA-87: 2.16.840.1.101.3.4.3.19
    private static final byte[] OID_ML_DSA_87 = { 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x03,
            0x13 };

    private static PrivateKey toPrivateKey(byte[] privateKey) throws InvalidKeySpecException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

            var oid = switch (privateKey.length) {
            case 2560 -> OID_ML_DSA_44;
            case 4032 -> OID_ML_DSA_65;
            case 4896 -> OID_ML_DSA_87;
            default -> throw new IllegalArgumentException("Unsupported key length: " + privateKey.length);
            };

            // 1. Construct AlgorithmIdentifier: SEQUENCE(OID)
            var algId = new ByteArrayOutputStream();
            algId.write(0x30);
            algId.write(oid.length);
            algId.write(oid);

            // 2. Construct PrivateKey (Double-wrapped OCTET STRING)
            // The KeyFactory expects: OCTET STRING( OCTET STRING( rawBytes ) )
            var innerKey = new ByteArrayOutputStream();
            innerKey.write(0x04); // Inner OCTET STRING tag
            writeDerLength(innerKey, privateKey.length);
            innerKey.write(privateKey);

            var outerKey = new ByteArrayOutputStream();
            outerKey.write(0x04); // Outer OCTET STRING tag
            writeDerLength(outerKey, innerKey.size());
            outerKey.write(innerKey.toByteArray());

            // 3. Construct final PrivateKeyInfo: SEQUENCE(Version, AlgId, OuterKey)
            var version = new byte[] { 0x02, 0x01, 0x00 }; // INTEGER 0
            int totalLen = version.length + algId.size() + outerKey.size();

            var pkcs8 = new ByteArrayOutputStream();
            pkcs8.write(0x30); // SEQUENCE
            writeDerLength(pkcs8, totalLen);
            pkcs8.write(version);
            pkcs8.write(algId.toByteArray());
            pkcs8.write(outerKey.toByteArray());

            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8.toByteArray()));

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeDerLength(ByteArrayOutputStream out, int length) throws IOException {
        if (length < 128) {
            out.write(length);
        } else {
            // Long form length
            int size = (length > 255) ? 2 : 1;
            out.write(0x80 | size);
            if (size == 2)
                out.write((length >> 8) & 0xFF);
            out.write(length & 0xFF);
        }
    }
}