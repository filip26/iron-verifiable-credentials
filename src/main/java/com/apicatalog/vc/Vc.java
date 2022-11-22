package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.SignatureSuiteProvider;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.key.KeyPair;
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
     * @throws DocumentError
     * @throws VerificationError
     */
    public static Verifier verify(final URI location, final SignatureSuiteProvider suiteProvider) throws DocumentError, VerificationError {
        return new Verifier(location, suiteProvider);
    }

    /**
     * Verifies VC/VP document data integrity and signature.
     *
     * @param document the document to verify
     *
     * @return {@link Verifier} allowing to set options and assert document's validity
     *
     * @throws DocumentError
     * @throws VerificationError
     */
    public static Verifier verify(final JsonObject document, final SignatureSuiteProvider suiteProvider) throws DocumentError, VerificationError {
        return new Verifier(document, suiteProvider);
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with added proof property.
     *
     * @param documentLocation
     * @param keyPair
     * @param options
     *
     * @return {@link Issuer} allowing to set options and to sign the given document
     *
     * @throws DocumentError
     * @throws SigningError
     */
    public static Issuer sign(URI documentLocation, KeyPair keyPair, final SignatureSuite signatureSuite) throws DocumentError, SigningError {
        return new Issuer(documentLocation, keyPair, signatureSuite);
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
     * @throws DocumentError
     * @throws SigningError
     */
    public static Issuer sign(JsonObject document, KeyPair keyPair, final SignatureSuite signatureSuite) throws DocumentError, SigningError {
        return new Issuer(document, keyPair, signatureSuite);
    }

    /**
     * Generates public/private key pair.
     *
     * @param signatureSuite a signature suite used to generate a key pair.
     *
     * @return {@link KeyGenError} allowing to set options and to generate key pair
     *
     * @throws KeyGenError
     */
    public static KeysGenerator generateKeysfinal(final SignatureSuite signatureSuite) throws KeyGenError {
    	if (signatureSuite == null) {
    		throw new IllegalArgumentException("The signatureSuite parameter must not be null.");
    	}
    	return new KeysGenerator(new LinkedDataSignature(signatureSuite));
    }
}
