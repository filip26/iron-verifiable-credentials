package com.apicatalog.ld.signature.ed25519;

import java.security.GeneralSecurityException;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.key.KeyPair;
import com.google.crypto.tink.signature.SignatureConfig;
import com.google.crypto.tink.subtle.Ed25519Sign;
import com.google.crypto.tink.subtle.Ed25519Verify;

public final class Ed25519Signature2020Provider implements SignatureAlgorithm {

    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        try {
            SignatureConfig.register();

            Ed25519Verify verifier = new Ed25519Verify(publicKey);

            verifier.verify(signature, data);

        } catch (GeneralSecurityException e) {
            throw new VerificationError(Code.InvalidSignature, e);
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
    public KeyPair keygen(int length) throws KeyGenError {

        try {
            Ed25519Sign.KeyPair kp = Ed25519Sign.KeyPair.newKeyPair();

            byte[] privateKey = kp.getPrivateKey();
            byte[] publicKey = kp.getPublicKey();

            final KeyPair keyPair = new KeyPair();
            keyPair.setType("https://w3id.org/security#Ed25519KeyPair2020");
            keyPair.setPrivateKey(privateKey);
            keyPair.setPublicKey(publicKey);
            
            return keyPair;
            
        } catch (GeneralSecurityException e) {
            throw new KeyGenError(e);
        }
    }
}
