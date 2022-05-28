package com.apicatalog.lds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.TinkConfig;
import com.google.crypto.tink.signature.SignatureConfig;
import com.google.crypto.tink.subtle.Ed25519Sign;

public class TinkSignature implements SignatureAlgorithm {

    @Override
    public boolean verify(byte[] publicKey, byte[] signature, byte[] data) {

        try {
            TinkConfig.register(); // TODO find proper place!

            // 1. Generate the private key material.
            KeysetHandle privateKeysetHandle = KeysetHandle.generateNew(KeyTemplates.get("ED25519"));

            KeysetHandle pp = privateKeysetHandle.getPublicKeysetHandle();

            ByteArrayOutputStream publicKeyStream = new ByteArrayOutputStream();

            try {
                CleartextKeysetHandle.write(pp, JsonKeysetWriter.withOutputStream(publicKeyStream));

                System.out.println(publicKeyStream.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            KeysetHandle publicKeysetHandle = KeysetHandle.readNoSecret(publicKey);

//        // 2. Get the primitive.
//        PublicKeySign signer = privateKeysetHandle.getPrimitive(PublicKeySign.class);
//
//        // 3. Use the primitive to sign.
//        byte[] signature = signer.sign(data);

            // VERIFYING

            // 2. Get the primitive.
            PublicKeyVerify verifier = publicKeysetHandle.getPrimitive(PublicKeyVerify.class);

            // 4. Use the primitive to verify.
            verifier.verify(signature, data);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        // TODO
        return false;
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) {

        try {
            // Register all signature key types with the Tink runtime.
            SignatureConfig.register();

            byte[] pk = Arrays.copyOfRange(privateKey, 4, privateKey.length);

            System.out.println("pk " + pk.length);
            System.out.println(Arrays.toString(pk));

            Ed25519Sign signer = new Ed25519Sign(pk);

            byte[] signature = signer.sign(data);
            System.out.println(Arrays.toString(signature));

            return signature;

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }
}
