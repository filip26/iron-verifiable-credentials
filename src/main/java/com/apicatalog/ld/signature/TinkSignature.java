package com.apicatalog.ld.signature;

import java.security.GeneralSecurityException;

import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.google.crypto.tink.signature.SignatureConfig;
import com.google.crypto.tink.subtle.Ed25519Sign;
import com.google.crypto.tink.subtle.Ed25519Verify;

public class TinkSignature implements SignatureAlgorithm {

    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        try {
            SignatureConfig.register();

            Ed25519Verify verifier = new Ed25519Verify(publicKey);

            verifier.verify(signature, data);

        } catch (GeneralSecurityException e) {
            throw new VerificationError(Code.InvalidSignature, e);     //TODO more details
        }        
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
