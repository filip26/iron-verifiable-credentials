package com.apicatalog.lds.primitive;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
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

import com.apicatalog.lds.SigningError;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.algorithm.SignatureAlgorithm;
import com.apicatalog.lds.key.KeyPair;

public class EdDsaSignature implements SignatureAlgorithm {

    private final String type;
    
    public EdDsaSignature(String type) {
        this.type = type;
    }

    @Override
    public boolean verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        
        try {            
            java.security.Signature suite = java.security.Signature.getInstance(type);

            suite.initVerify(getPublicKey(publicKey));
            suite.update(data);

            return suite.verify(signature);

        } catch (InvalidParameterSpecException | InvalidKeySpecException 
                | InvalidKeyException | NoSuchAlgorithmException 
                | SignatureException e) {
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
 
        } catch (InvalidParameterSpecException | InvalidKeySpecException  
                | InvalidKeyException | NoSuchAlgorithmException 
                | SignatureException e) {
            throw new SigningError(e);
        }
    }    
    
    @Override
    public KeyPair keygen(int length) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(type);
            keyGen.initialize(length);
            
            java.security.KeyPair kp =  keyGen.generateKeyPair();

            //TODO
            
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    private PublicKey getPublicKey(byte[] publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {

        KeyFactory kf = KeyFactory.getInstance(type);
        
        // determine if x was odd.
        boolean xisodd = false;
        int lastbyteInt = publicKey[publicKey.length - 1];
        if ((lastbyteInt & 255) >> 7 == 1) {
            xisodd = true;
        }
        
        // make sure most significant bit will be 0 - after reversing.
        publicKey[publicKey.length - 1] &= 127;
              
        publicKey = reverse(publicKey);
        BigInteger y = new BigInteger(1, publicKey);

        NamedParameterSpec paramSpec = new NamedParameterSpec("Ed25519");
        EdECPoint ep = new EdECPoint(xisodd, y);
        EdECPublicKeySpec pubSpec = new EdECPublicKeySpec(paramSpec, ep);
        PublicKey pub = kf.generatePublic(pubSpec);
        return pub;
    }

    private PrivateKey getPrivateKey(byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {
        KeyFactory kf = KeyFactory.getInstance("Ed25519");
        
        NamedParameterSpec paramSpec = new NamedParameterSpec(type);
        EdECPrivateKeySpec spec = new EdECPrivateKeySpec(paramSpec, privateKey);
        return kf.generatePrivate(spec);
    }
    
    private final static byte[] reverse(byte[] data) {
        final byte[] reversed = new byte[data.length];
        for (int i=0; i<data.length; i++) {
            reversed[data.length - i - 1] = data[i];
        }   
        return reversed;
    }
}
