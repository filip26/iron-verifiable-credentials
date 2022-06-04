package com.apicatalog.vc.api;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.lds.DataError;
import com.apicatalog.lds.DataError.ErrorType;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.SigningError;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.proof.ProofOptions;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.Verifiable;

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
     * @param length
     * @return
     */
    //FIXMe use processor api allowing to set length
    public static KeyPair generateKeys(String type) {

        //TODO reject unknown keypair type
        final LinkedDataSignature lds = new LinkedDataSignature(new Ed25519Signature2020());

        return lds.keygen(256); //FIXME
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
            // validate the presentation object
            //TODO
        }

        // is not expanded JSON-LD object
        if (!JsonLdUtils.hasType(expanded)) {
            throw new DataError(ErrorType.Missing, Keywords.TYPE);
        }
        throw new DataError(ErrorType.Unknown, Keywords.TYPE);
    }

}
