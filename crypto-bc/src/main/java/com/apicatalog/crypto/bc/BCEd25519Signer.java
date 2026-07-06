package com.apicatalog.crypto.bc;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.NamedParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class BCEd25519Signer {

    private final PrivateKey privateKey;

    public BCEd25519Signer(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public static BCEd25519Signer newInstance(byte[] privateKey) {
        try {
            return new BCEd25519Signer(getPrivateKey(privateKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidParameterSpecException e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] sign(byte[] data) throws SignatureException {
        try {
            var signer = Signature.getInstance("Ed25519");
            signer.initSign(privateKey);
            signer.update(data);
            return signer.sign();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PrivateKey getPrivateKey(byte[] privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {

        var keyFactory = KeyFactory.getInstance("Ed25519", new BouncyCastleProvider());

        NamedParameterSpec paramSpec = new NamedParameterSpec(keyFactory.getAlgorithm());
        EdECPrivateKeySpec spec = new EdECPrivateKeySpec(paramSpec, privateKey);
        return keyFactory.generatePrivate(spec);
    }
}