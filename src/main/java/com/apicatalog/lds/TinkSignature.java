package com.apicatalog.lds;

import java.security.GeneralSecurityException;

import com.apicatalog.lds.algorithm.SignatureAlgorithm;
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
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) {

        try {
            // Register all signature key types with the Tink runtime.
            SignatureConfig.register();

            Ed25519Sign signer = new Ed25519Sign(privateKey);

            byte[] signature = signer.sign(data);

            return signature;

        } catch (GeneralSecurityException e) {
            e.printStackTrace();        //TODO
        }

        return null;
    }
    

    /*TODO keygen
     *      
                    Ed25519Sign.KeyPair keyPair = Ed25519Sign.KeyPair.newKeyPair();
            privateKey = keyPair.getPrivateKey();
    System.out.println("private= " + Multibase.encode(Multicodec.encode(Codec.Ed25519PrivateKey, privateKey))); 
    byte[] publicKey = keyPair.getPublicKey();
System.out.println("public= " + Multibase.encode(Multicodec.encode(Codec.Ed25519PublicKey, publicKey)));            

     */
}
