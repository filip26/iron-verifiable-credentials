package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.SigningError;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.proof.ProofOptions;

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
    public static VerificationProcessor verify(URI location) throws DataIntegrityError, VerificationError {
        return new VerificationProcessor(location);
    }

    /**
     * Verifies VC/VP document data integrity and signature.
     * 
     * @param location
     * @throws DataIntegrityError
     * @throws VerificationError
     */
    public static VerificationProcessor verify(JsonObject document) throws DataIntegrityError, VerificationError {
        return new VerificationProcessor(document);
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with added proof property.
     * 
     * @param documentLocation
     * @param keyPairLocation
     * @param options
     * @return signed VC/VP with proof property at the root level
     * @throws DataIntegrityError
     * @throws VerificationError
     */
    public static SigningProcessor sign(URI documentLocation, URI keyPairLocation, ProofOptions options) throws DataIntegrityError, SigningError {
        return new SigningProcessor(documentLocation, keyPairLocation, options);
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
