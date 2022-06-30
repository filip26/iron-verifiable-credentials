package com.apicatalog.ld.signature.ed25519;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.NamedParameterSpec;

import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;

public class SunSignatureProvider implements SignatureAlgorithm {

    private final String type;

    public SunSignatureProvider(String type) {
        this.type = type;
    }

    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        try {
            java.security.Signature suite = java.security.Signature.getInstance(type);

            suite.initVerify(getPublicKey(publicKey));
            suite.update(data);

            if (!suite.verify(signature)) {
                throw new VerificationError(Code.InvalidSignature);     //TODO more details
            }

        } catch (InvalidParameterSpecException | InvalidKeySpecException | InvalidKeyException
                | NoSuchAlgorithmException | SignatureException e) {
            throw new VerificationError(e);
        }
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {

        try {
            java.security.Signature suite = java.security.Signature.getInstance(type);

            suite.initSign(getPrivateKey(privateKey));
            suite.update(data);

            return suite.sign();

        } catch (InvalidParameterSpecException | InvalidKeySpecException | InvalidKeyException
                | NoSuchAlgorithmException | SignatureException e) {
            throw new SigningError(e);
        }
    }

    @Override
    public KeyPair keygen(final int length) {
        throw new UnsupportedOperationException();
    }

    private PublicKey getPublicKey(final byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {

        final KeyFactory kf = KeyFactory.getInstance(type);

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

        NamedParameterSpec paramSpec = new NamedParameterSpec(type);
        EdECPoint ep = new EdECPoint(xisodd, y);
        EdECPublicKeySpec pubSpec = new EdECPublicKeySpec(paramSpec, ep);
        PublicKey pub = kf.generatePublic(pubSpec);
        return pub;
    }

    private PrivateKey getPrivateKey(byte[] privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {
        KeyFactory kf = KeyFactory.getInstance(type);

        NamedParameterSpec paramSpec = new NamedParameterSpec(type);
        EdECPrivateKeySpec spec = new EdECPrivateKeySpec(paramSpec, privateKey);
        return kf.generatePrivate(spec);
    }

    private final static byte[] reverse(byte[] data) {
        final byte[] reversed = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            reversed[data.length - i - 1] = data[i];
        }
        return reversed;
    }
}
