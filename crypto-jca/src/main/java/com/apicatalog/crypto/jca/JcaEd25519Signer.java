package com.apicatalog.crypto.jca;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;

public final class JcaEd25519Signer {

    private PrivateKey privateKey;

    public JcaEd25519Signer(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public static JcaEd25519Signer newInstance(byte[] privateKey) throws InvalidKeySpecException {
        return new JcaEd25519Signer(toPrivateKey(privateKey));
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

    /**
     * Loads Ed25519 from 32-byte raw format.
     * @throws InvalidKeySpecException 
     */
    private static PrivateKey toPrivateKey(byte[] rawPrivateKey) throws InvalidKeySpecException {
        try {
            var keyFactory = KeyFactory.getInstance("Ed25519");
            
            // Construct the spec for Ed25519 using the raw byte array directly
            NamedParameterSpec paramSpec = NamedParameterSpec.ED25519;
            var spec = new EdECPrivateKeySpec(paramSpec, rawPrivateKey);

            return keyFactory.generatePrivate(spec);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
