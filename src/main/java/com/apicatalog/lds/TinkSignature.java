package com.apicatalog.lds;

import java.security.GeneralSecurityException;

import com.apicatalog.lds.algorithm.SignatureAlgorithm;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.google.crypto.tink.signature.SignatureConfig;
import com.google.crypto.tink.subtle.Ed25519Sign;
import com.google.crypto.tink.subtle.Ed25519Verify;

public class TinkSignature implements SignatureAlgorithm {

    @Override
    public boolean verify(byte[] publicKey, byte[] signature, byte[] data) {

        try {
            SignatureConfig.register();

            Ed25519Verify verifier = new Ed25519Verify(publicKey);

            verifier.verify(signature, data);
            return true;

        } catch (GeneralSecurityException e) {
            /* ignore */
        }
        return false;
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {

        try {
            // Register all signature key types with the Tink runtime.
            SignatureConfig.register();

            Ed25519Sign signer = new Ed25519Sign(privateKey);

            byte[] signature = signer.sign(data);

            return signature;

        } catch (GeneralSecurityException e) {
            throw new SigningError(e);
        }
    }
    
    @Override
    public KeyPair keygen(int length) {

        try {
            Ed25519Sign.KeyPair kp = Ed25519Sign.KeyPair.newKeyPair();
            
            byte[] privateKey = kp.getPrivateKey();
            byte[] publicKey = kp.getPublicKey();

            return new KeyPair(publicKey, privateKey);
            
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //TODO
        return null;
        
    }
}
