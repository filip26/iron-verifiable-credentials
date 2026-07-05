package com.apicatalog.crypto.bc;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.NamedParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class BcEd25519Verifier {

    private static final BcEd25519Verifier INSTANCE = new BcEd25519Verifier();

    public static BcEd25519Verifier getInstance() {
        return INSTANCE;
    }

    public boolean verify(final byte[] publicKey, final byte[] data, final byte[] signature)
            throws SignatureException, InvalidKeyException {

        try {
            var verifier = Signature.getInstance("Ed25519");

            verifier.initVerify(getPublicKeyFromBytes(publicKey));
            verifier.update(data);

            return verifier.verify(signature);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);

        } catch (InvalidKeySpecException | InvalidKeyException | InvalidParameterSpecException e) {
            throw new InvalidKeyException(e);

        }
    }

    private static PublicKey getPublicKeyFromBytes(final byte[] publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {

        var keyFactory = KeyFactory.getInstance("Ed25519", new BouncyCastleProvider());

        // determine if x was odd.
        boolean xisodd = false;

        int lastbyteInt = publicKey[publicKey.length - 1];

        if ((lastbyteInt & 255) >> 7 == 1) {
            xisodd = true;
        }

        // make public key copy
        byte[] key = new byte[publicKey.length];
        System.arraycopy(publicKey, 0, key, 0, key.length);

        // make sure most significant bit will be 0 - after reversing.
        key[key.length - 1] &= 127;

        key = reverse(key);

        BigInteger y = new BigInteger(1, key);

        NamedParameterSpec paramSpec = new NamedParameterSpec(keyFactory.getAlgorithm());
        EdECPoint ep = new EdECPoint(xisodd, y);
        EdECPublicKeySpec pubSpec = new EdECPublicKeySpec(paramSpec, ep);
        return keyFactory.generatePublic(pubSpec);
    }

    private static byte[] reverse(byte[] data) {
        final byte[] reversed = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            reversed[data.length - i - 1] = data[i];
        }
        return reversed;
    }
}