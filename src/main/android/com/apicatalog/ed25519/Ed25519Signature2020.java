package com.apicatalog.ed25519;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import com.apicatalog.vc.VerificationError;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.config.TinkConfig;

public class Ed25519Signature2020Android {

    static {
    }


    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {

        try {
            TinkConfig.register();      //TODO find proper place!



        // 1. Generate the private key material.
        KeysetHandle privateKeysetHandle = KeysetHandle.generateNew(
            KeyTemplates.get("ECDSA_P256"));

        KeysetHandle pp = privateKeysetHandle.getPublicKeysetHandle();

        ByteArrayOutputStream publicKeyStream = new ByteArrayOutputStream();

        try {
            CleartextKeysetHandle.write(pp,
                JsonKeysetWriter.withOutputStream(publicKeyStream));

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
        //TODO
    }

    /* Google Tink Public Key Format
     * {
   "primaryKeyId":1162504282,
   "key":[
      {
         "keyData":{
            "typeUrl":"type.googleapis.com/google.crypto.tink.EcdsaPublicKey",
            "value":"EgYIAxACGAIaIQDFvf/eZLgLMGyjiy+9Bbl3jtTuqOMznARjzxdL4tthNyIgBy1cn/K/MsKhIoFDLaYHhicyw0bBYrzUS8LXdFF65o8=",
            "keyMaterialType":"ASYMMETRIC_PUBLIC"
         },
         "status":"ENABLED",
         "keyId":1162504282,
         "outputPrefixType":"TINK"
      }
   ]
}
     */

}
