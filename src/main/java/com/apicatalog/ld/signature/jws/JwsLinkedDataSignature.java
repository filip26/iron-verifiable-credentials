package com.apicatalog.ld.signature.jws;

import com.apicatalog.ld.signature.*;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Based on {@link LinkedDataSignature}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JwsLinkedDataSignature {

    private final JwsSignatureSuite suite;

    public JwsLinkedDataSignature(JwsSignatureSuite suite) {
        this.suite = suite;
    }

    /**
     * Verifies the given signed VC/VP document.
     *
     * @see <a href="https://w3c-ccg.github.io/data-integrity-spec/#proof-verification-algorithm">Verification Algorithm</a>
     *
     * @param document expanded unsigned VC/VP document
     * @param proof expanded proof
     * @param publicKey JWK public key
     * @param jws JWS (Json Web Signature) with unencoded (detached) payload
     *
     * @throws VerificationError error while verifying
     */
    public boolean verify(final JsonObject document, final JsonObject proof, final JWK publicKey, String jws) throws VerificationError {
        final JsonObject proofObject = JwsEmbeddedProofAdapter.removeProofValue(proof);
        try {
            final byte[] documentHash = hashCode(document, proofObject);
            return suite.verify(publicKey, jws, documentHash);
        } catch (LinkedDataSuiteError e) {
            throw new VerificationError(e);
        }
    }

    /**
     * Signs the given VC/VP document and returns the document signature.
     *
     * @see <A href="https://w3c-ccg.github.io/data-integrity-spec/#proof-algorithm">Proof Algorithm</a>
     *
     * @param document expanded unsigned VC/VP document
     * @param privateKey JWK private key
     * @param options expanded proof options (proof with no proofValue)
     *
     * @return JWS (Json Web Signature) with unencoded (detached) payload
     *
     * @throws SigningError error while signing
     */
    public String sign(JsonObject document, final JWK privateKey, JsonObject options) throws SigningError {
        try {
            final byte[] documentHash = hashCode(document, options);
            return suite.sign(privateKey, documentHash);
        } catch (LinkedDataSuiteError e) {
            throw new SigningError(e);
        }
    }

    /**
     * Verifies the given signed VC/VP document.
     *
     * @see <a href="https://w3c-ccg.github.io/data-integrity-spec/#proof-verification-algorithm">Verification Algorithm</a>
     *
     * @param document expanded unsigned VC/VP document
     * @param proof expanded proof
     * @param publicKey public key
     * @param keyId key id (may be null, if null random uuid is assigned)
     * @param jws JWS (Json Web Signature) with unencoded (detached) payload
     *
     * @throws VerificationError error while verifying
     */
    public boolean verify(final JsonObject document, final JsonObject proof, final PublicKey publicKey, String keyId, String jws) throws VerificationError {
        final JsonObject proofObject = JwsEmbeddedProofAdapter.removeProofValue(proof);
        try {
            final byte[] documentHash = hashCode(document, proofObject);
            return suite.verify(publicKey, keyId, jws, documentHash);
        } catch (LinkedDataSuiteError e) {
            throw new VerificationError(e);
        }
    }

    /**
     * Signs the given VC/VP document and returns the document signature.
     *
     * @see <A href="https://w3c-ccg.github.io/data-integrity-spec/#proof-algorithm">Proof Algorithm</a>
     *
     * @param document expanded unsigned VC/VP document
     * @param privateKey private key
     * @param publicKey public key (also needed in order to create Json Web Key)
     * @param keyId key id (may be null, if null random uuid is assigned)
     * @param options expanded proof options (proof with no proofValue)
     *
     * @return JWS (Json Web Signature) with unencoded (detached) payload
     *
     * @throws SigningError error while signing
     */
    public String sign(JsonObject document, final PrivateKey privateKey, final PublicKey publicKey, String keyId, JsonObject options) throws SigningError {
        try {
            final byte[] documentHash = hashCode(document, options);
            return suite.sign(privateKey, publicKey, keyId, documentHash);
        } catch (LinkedDataSuiteError e) {
            throw new SigningError(e);
        }
    }

    /**
     * @see <a href="https://w3c-ccg.github.io/data-integrity-spec/#create-verify-hash-algorithm">Hash Algorithm</a>
     *
     * @param document expanded unsigned VC/VP document
     * @param proof expanded proof options (proof with no proofValue)
     *
     * @return computed hash code
     *
     * @throws LinkedDataSuiteError
     */  //would be nice to leave this public so sign/verify functions can be overridden with own APIs (non-nimbus)
    public byte[] hashCode(JsonStructure document, JsonObject proof) throws LinkedDataSuiteError {

        byte[] proofOptionsHash = suite.digest(suite.canonicalize(proof));
        byte[] documentHash = suite.digest(suite.canonicalize(document));

        // proof hash + document hash
        byte[] payload = new byte[proofOptionsHash.length + documentHash.length];

        System.arraycopy(proofOptionsHash, 0, payload, 0, proofOptionsHash.length);
        System.arraycopy(documentHash, 0, payload, proofOptionsHash.length, documentHash.length);

        return payload;
    }

    /**
     * Generate JWK (Json Web Key) key pair [within SW]
     *
     * @return JWK (with public and private key attributes)
     * @throws KeyGenError thrown in case generation fails
     */
    public JWK keygen(/*URI id, int length*/) throws KeyGenError {
        return suite.keygen(/*length*/);
    }

}