package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.SignatureSuiteMapper;
import com.apicatalog.ld.signature.SignatureSuiteProvider;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.proof.Proof;
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
     * @return {@link Verifier} allowing to set options and assert document's
     *         validity
     *
     * @throws DocumentError
     * @throws VerificationError
     */
    public static Verifier verify(final URI location, final SignatureSuiteProvider suiteProvider) throws DocumentError, VerificationError {
        return new Verifier(location, suiteProvider);
    }

    public static Verifier verify(final URI location, final SignatureSuite suite) throws DocumentError, VerificationError {
        return new Verifier(location, new SignatureSuiteMapper().add(suite));
    }

    /**
     * Verifies VC/VP document data integrity and signature.
     *
     * @param document the document to verify
     *
     * @return {@link Verifier} allowing to set options and assert document's
     *         validity
     *
     * @throws DocumentError
     * @throws VerificationError
     */
    public static Verifier verify(final JsonObject document, final SignatureSuiteProvider suiteProvider) throws DocumentError, VerificationError {
        return new Verifier(document, suiteProvider);
    }

    public static Verifier verify(final JsonObject document, final SignatureSuite suite) throws DocumentError, VerificationError {
        return new Verifier(document, new SignatureSuiteMapper().add(suite));
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with an new proof
     *
     * @param documentLocation
     * @param keyPair
     * @param draft a draft of the proof to sign and attach
     *
     * @return {@link Issuer} allowing to set options and to sign the given document
     *
     * @throws DocumentError
     * @throws SigningError
     */
    public static Issuer sign(URI documentLocation, KeyPair keyPair, final Proof draft) throws DocumentError, SigningError {
        return new Issuer(documentLocation, keyPair, draft);
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with a new proof
     *
     * @param document
     * @param keyPair
     * @param draft a draft of the proof to sign and attach
     *
     * @return {@link Issuer} allowing to set options and to sign the given document
     *
     * @throws DocumentError
     * @throws SigningError
     */
    public static Issuer sign(JsonObject document, KeyPair keyPair, final Proof draft) throws DocumentError, SigningError {
        return new Issuer(document, keyPair, draft);
    }

    /**
     * Generates public/private key pair.
     *
     * @param cryptoSuite a crypto suite used to generate a key pair.
     *
     * @return {@link KeyGenError} allowing to set options and to generate key pair
     *
     * @throws KeyGenError
     */
    public static KeysGenerator generateKeysfinal(final CryptoSuite cryptoSuite) throws KeyGenError {
        if (cryptoSuite == null) {
            throw new IllegalArgumentException("The cryptoSuite parameter must not be null.");
        }
        return new KeysGenerator(new LinkedDataSignature(cryptoSuite));
    }
}
