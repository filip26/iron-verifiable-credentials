package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.SigningError;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.proof.ProofOptions;

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
     * @throws DataIntegrityError
     * @throws VerificationError
     */
    public static VerificationApi verify(URI location) throws DataIntegrityError, VerificationError {
        return new VerificationApi(location);
    }

    /**
     * Verifies VC/VP document data integrity and signature.
     * 
     * @param location
     * @throws DataIntegrityError
     * @throws VerificationError
     */
    public static VerificationApi verify(JsonObject document) throws DataIntegrityError, VerificationError {
        return new VerificationApi(document);
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with added proof property.
     * 
     * @param documentLocation
     * @param keyPairLocation
     * @param options
     * @param loader
     * @return signed VC/VP with proof property at the root level
     * @throws DataIntegrityError
     * @throws VerificationError
     */
    //TODO use SignApi - provide getCompacted() method getExpanded() -> default now
    public static JsonObject sign(URI documentLocation, URI keyPairLocation, ProofOptions options,
            DocumentLoader loader) throws DataIntegrityError, SigningError {
        try {
            // load the document
            final JsonArray document = JsonLd.expand(documentLocation).loader(loader).get();

            // load key pair
            Document keys = loader.loadDocument(keyPairLocation, new DocumentLoaderOptions());

            // TODO keyPair type must match options.type
            Ed25519KeyPair2020 keyPair = Ed25519KeyPair2020.from(keys.getJsonContent().orElseThrow().asJsonObject()); // FIXME

            LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());

            JsonObject signed = signature.sign(document.getJsonObject(0), options, keyPair);

            return signed;

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    /**
     * Generates public/private key pair. 
     * 
     * @param type requested key pair type, e.g. <code>https://w3id.org/security#Ed25519KeyPair2020</code>
     * @param length
     * @return
     */
    public static KeyPair generateKeys(String type, int length) {
        
        //TODO reject unknown keypair type 

        return new Ed25519Signature2020().keygen(length);
    }
    
}
