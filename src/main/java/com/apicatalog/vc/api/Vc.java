package com.apicatalog.vc.api;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.KeyGenError.Code;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonObject;

/**
 * High level API to process Verifiable Credentials (VC) and Presentations (VP).
 */
public final class Vc {

    /**
     * Verifies VC/VP document data integrity and signature.
     *
     * @param location
     * @throws DataError
     * @throws VerificationError
     */
    public static VerifierApi verify(URI location) throws DataError, VerificationError {
        return new VerifierApi(location);
    }

    /**
     * Verifies VC/VP document data integrity and signature.
     *
     * @param location
     * @throws DataError
     * @throws VerificationError
     */
    public static VerifierApi verify(JsonObject document) throws DataError, VerificationError {
        return new VerifierApi(document);
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with added proof property.
     *
     * @param documentLocation
     * @param keyPairLocation
     * @param options
     * @return signed VC/VP with proof property at the root level
     * @throws DataError
     * @throws VerificationError
     */
    public static IssuerApi sign(URI documentLocation, URI keyPairLocation, ProofOptions options) throws DataError, SigningError {
        return new IssuerApi(documentLocation, keyPairLocation, options);
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with added proof property.
     *
     * @param document
     * @param keyPair
     * @param options
     * @return signed VC/VP with proof property at the root level
     * @throws DataError
     * @throws VerificationError
     */
    public static IssuerApi sign(JsonObject document, KeyPair keyPair, ProofOptions options) throws DataError, SigningError {
        return new IssuerApi(document, keyPair, options);
    }

    /**
     * Generates public/private key pair.
     *
     * @param type requested key pair type, e.g. <code>https://w3id.org/security#Ed25519KeyPair2020</code>
     * @return
     */
    public static KeyGenApi generateKeys(String type) throws KeyGenError {
        
        if (Ed25519Signature2020.isTypeOf(type)) {
            final LinkedDataSignature lds = new LinkedDataSignature(new Ed25519Signature2020());

            return new KeyGenApi(lds);
        }   
        throw new KeyGenError(Code.UnknownCryptoSuite);
    }

    protected static Verifiable get(JsonObject expanded) throws DataError {
        // is a credential?
        if (Credential.isCredential(expanded)) {

            final JsonObject object = expanded.asJsonObject();

            // validate the credential object
            final Credential credential = Credential.from(object);

            return credential;
        }

        // is a presentation?
        if (Presentation.isPresentation(expanded)) {

            final JsonObject object =expanded.asJsonObject();

            // validate the presentation object
            final Presentation presentation = Presentation.from(object);

            return presentation;
        }

        // is not expanded JSON-LD object
        if (!JsonLdUtils.hasType(expanded)) {
            throw new DataError(ErrorType.Missing, Keywords.TYPE);
        }

        throw new DataError(ErrorType.Unknown, Keywords.TYPE);
    }
}
