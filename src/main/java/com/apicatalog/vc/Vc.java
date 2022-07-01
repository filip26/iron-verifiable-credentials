package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.KeyGenError.Code;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.processor.Issuer;
import com.apicatalog.vc.processor.KeysGenerator;
import com.apicatalog.vc.processor.Verifier;

import jakarta.json.JsonObject;

/**
 * High level API to process Verifiable Credentials (VC) and Presentations (VP).
 */
public final class Vc {

    /**
     * Verifies VC/VP document data integrity and signature.
     *
     * @param location a location of the document to verify
     * @return {@link Verifier} allowing to set options and assert document's validity
     * 
     * @throws DataError
     * @throws VerificationError
     */
    public static Verifier verify(URI location) throws DataError, VerificationError {
        return new Verifier(location);
    }

    /**
     * Verifies VC/VP document data integrity and signature.
     *
     * @param document the document to verify
     * 
     * @return {@link Verifier} allowing to set options and assert document's validity
     *  
     * @throws DataError
     * @throws VerificationError
     */
    public static Verifier verify(JsonObject document) throws DataError, VerificationError {
        return new Verifier(document);
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with added proof property.
     *
     * @param documentLocation
     * @param keyPairLocation
     * @param options
     * 
     * @return {@link Issuer} allowing to set options and to sign the given document
     * 
     * @throws DataError
     * @throws SigningError 
     */
    public static Issuer sign(URI documentLocation, URI keyPairLocation, ProofOptions options) throws DataError, SigningError {
        return new Issuer(documentLocation, keyPairLocation, options);
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with added proof property.
     *
     * @param document
     * @param keyPair
     * @param options
     * 
     * @return {@link Issuer} allowing to set options and to sign the given document
     * 
     * @throws DataError
     * @throws SigningError 
     */
    public static Issuer sign(JsonObject document, KeyPair keyPair, ProofOptions options) throws DataError, SigningError {
        return new Issuer(document, keyPair, options);
    }

    /**
     * Generates public/private key pair.
     *
     * @param type requested key pair type, e.g. <code>https://w3id.org/security#Ed25519KeyPair2020</code>
     * 
     * @return {@link KeyGenError} allowing to set options and to generate key pair
     * 
     * @throws KeyGenError 
     */
    public static KeysGenerator generateKeys(String type) throws KeyGenError {

        if (Ed25519Signature2020.isTypeOf(type)) {
            final LinkedDataSignature lds = new LinkedDataSignature(new Ed25519Signature2020());

            return new KeysGenerator(lds);
        }
        throw new KeyGenError(Code.UnknownCryptoSuite);
    }
}
