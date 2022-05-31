package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.VerificationKey;
import com.apicatalog.lds.proof.EmbeddedProof;
import com.apicatalog.lds.proof.ProofOptions;
import com.apicatalog.vc.VerificationError.Code;

import jakarta.json.JsonArray;
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
    public static boolean verify(URI location, DocumentLoader loader) throws DataIntegrityError, VerificationError {
        /*FIXME use VerificationApi, make loader optional - use default*/
        try {
            // load the document
            final JsonObject document  = JsonLd.expand(location).loader(loader).get().getJsonObject(0);  //FIXME

            // data integrity check
            EmbeddedProof proof = EmbeddedProof.from(document, loader);
            
            // check proof type
            if (!Ed25519Signature2020.TYPE.equals(proof.getType())) {
                throw new VerificationError(Code.UnknownCryptoSuiteType);
            }

            VerificationKey verificationMethod = get(proof.getVerificationMethod().getId(), loader);
            
            LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());    //FIXME check keypair type
            
            return signature.verify(document, verificationMethod, proof.getValue()); 
                        
        } catch (JsonLdError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return false;
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

        //TODO keyPair type must match options.type
        
        try {
            // load the document
            final JsonArray  document  = JsonLd.expand(documentLocation).loader(loader).get();

            // load key pair
            Document keys = loader.loadDocument(keyPairLocation, new DocumentLoaderOptions());

            Ed25519KeyPair2020 keyPair = Ed25519KeyPair2020.from(keys.getJsonContent().orElseThrow().asJsonObject()); //FIXME

            LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());    //FIXME check keypair type

            JsonObject signed = signature.sign(document.getJsonObject(0), options, keyPair);

            return signed;

        } catch (JsonLdError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        return JsonObject.EMPTY_JSON_OBJECT;
    }

    // refresh/fetch verification method
    static final VerificationKey get(URI id, DocumentLoader loader) {

        try {
            final Document document = loader.loadDocument(id, new DocumentLoaderOptions());

            JsonObject method = document.getJsonContent().orElseThrow().asJsonObject();

            //TODO check  verification method type
            return Ed25519KeyPair2020.from(method);
        
        } catch (JsonLdError | DataIntegrityError e) {
            e.printStackTrace();
            //TODO
        }

        return null;
    }
}
