package com.apicatalog.vc;

import java.net.URI;
import java.util.Arrays;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.ProofOptions;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;

import jakarta.json.JsonObject;

/**
 * High level API to process Verified Credentials and Verified Presentations.
 *
 */
public final class Vc {

    /**
     * Verifies VC/VP document data integrity and signature.
     * 
     * @param location
     * @param loader
     * @throws DataIntegrityError
     * @throws VerificationError
     */
    public static /*FIXME use VerificationApi, make loader optional - use default*/ void verify(URI location, DocumentLoader loader) throws DataIntegrityError, VerificationError {

        final VcDocument data  = VcDocument.load(location, loader);

        if (data == null || !data.isVerifiable()) {
            throw new VerificationError();                  //TODO
        }

        data.asVerifiable().verify();
    }

    /**
     * Signs VC/VP document with using the provided signature suite. 

     * @param documentLocation
     * @param suite
     * @param loader
     * @return signed VC/VP with proof property at the root level
     * @throws DataIntegrityError 
     * @throws VerificationError 
     */
    public static JsonObject sign(URI documentLocation, URI keyPairLocation, ProofOptions options, DocumentLoader loader) throws DataIntegrityError, VerificationError {

        try {
            // load the document
            final VcDocument document  = VcDocument.load(documentLocation, loader);

            // load key pair
            Document keys = loader.loadDocument(keyPairLocation, new DocumentLoaderOptions());

            Ed25519KeyPair2020 keyPair = Ed25519KeyPair2020.from(keys.getJsonContent().orElseThrow().asJsonObject()); //FIXME

            byte[] vk = keyPair.getPublicKey();
            byte[] pk = keyPair.getPrivateKey();
            
            System.out.println(vk.length + " - " + Arrays.toString(vk));
            System.out.println(pk.length + " - " +Arrays.toString(pk));
            
            System.out.println(": " + Arrays.equals(vk,  0, 1, pk, 0, 1));
            System.out.println(": " + Arrays.equals(vk,  0, 1, pk, 33, 34));
            System.out.println(": " + Arrays.equals(vk,  0, 34, pk, 0, 32));
            System.out.println(": " + Arrays.equals(vk,  0, 34, pk, 34, 66));
            
            LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());    //FIXME check keypair type

            signature.sign(document.getExpandedDocument(), options, keyPair.getPrivateKey());
            //TODO

        } catch (JsonLdError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        return JsonObject.EMPTY_JSON_OBJECT;
    }
}
