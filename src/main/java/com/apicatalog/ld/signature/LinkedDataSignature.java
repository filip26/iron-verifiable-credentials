package com.apicatalog.ld.signature;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.key.VerificationKey;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class LinkedDataSignature {

    private final CryptoSuite suite;

    public LinkedDataSignature(CryptoSuite suite) {
        this.suite = suite;
    }

    /**
     * Verifies the given signed VC/VP document.
     *
     * @see <a href=
     *      "https://w3c-ccg.github.io/data-integrity-spec/#proof-verification-algorithm">Verification
     *      Algorithm</a>
     *
     * @param document        expanded unsigned VC/VP document
     * @param unsignedProof   expanded proof with no proofValue
     * @param verificationKey
     * @param signature
     *
     * @throws VerificationError
     * @throws DocumentError
     */
    public void verify(
            final JsonObject document,
            final JsonObject unsignedProof,
            final VerificationKey verificationKey,
            final byte[] signature) throws VerificationError, DocumentError {

        if (verificationKey == null || verificationKey.publicKey() == null) {
            throw new DocumentError(ErrorType.Missing, "ProofVerificationKey");
        }
        if (signature == null) {
            throw new DocumentError(ErrorType.Missing, "ProofValue");
        }

        try {
            final byte[] computeSignature = hashCode(document, unsignedProof);

            suite.verify(verificationKey.publicKey(), signature, computeSignature);

        } catch (LinkedDataSuiteError e) {
            throw new VerificationError(Code.LinkedDataSignature, e);
        }
    }

    /**
     * Issues the given VC/VP document and returns the document signature.
     *
     * @see <A href=
     *      "https://w3c-ccg.github.io/data-integrity-spec/#proof-algorithm">Proof
     *      Algorithm</a>
     *
     * @param document expanded unsigned VC/VP document
     * @param keyPair
     * @param proof
     *
     * @return computed signature
     *
     * @throws SigningError
     * @throws DocumentError
     */
    public byte[] sign(JsonObject document, KeyPair keyPair, JsonObject proof) throws SigningError {

        try {
            final byte[] documentHashCode = hashCode(document, proof);

            return suite.sign(keyPair.privateKey(), documentHashCode);

        } catch (LinkedDataSuiteError e) {
            throw new SigningError(SigningError.Code.LinkedDataSignature, e);
        }
    }

    /**
     * @see <a href=
     *      "https://w3c-ccg.github.io/data-integrity-spec/#create-verify-hash-algorithm">Hash
     *      Algorithm</a>
     *
     * @param document expanded unsigned VC/VP document
     * @param proof    expanded proof with no proofValue
     *
     * @return computed hash code
     *
     * @throws LinkedDataSuiteError
     */
    byte[] hashCode(JsonStructure document, JsonObject proof) throws LinkedDataSuiteError {

        byte[] proofHash = suite.digest(suite.canonicalize(proof));

        byte[] documentHash = suite.digest(suite.canonicalize(document));

        // proof hash + document hash
        byte[] result = new byte[proofHash.length + documentHash.length];

        System.arraycopy(proofHash, 0, result, 0, proofHash.length);
        System.arraycopy(documentHash, 0, result, proofHash.length, documentHash.length);

        return result;
    }

    public KeyPair keygen(URI id, int length) throws KeyGenError {
        return suite.keygen(length);
    }
}
