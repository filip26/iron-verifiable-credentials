package com.apicatalog.cryptosuite;

import java.util.Objects;

import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedTree;

public class Signature {
    
    private final CryptoSuite suite;

    public Signature(CryptoSuite suite) {
        this.suite = suite;
    }

    /**
     * Verifies the given signed VC/VP document.
     *
     * @see <a href=
     *      "https://w3c-ccg.github.io/data-integrity-spec/#proof-verification-algorithm">Verification
     *      Algorithm</a>
     *
     * @param expanded        expanded unsigned VC/VP document
     * @param unsignedProof   expanded proof with no proofValue
     * @param verificationKey
     * @param signature
     *
     * @throws VerificationError
     * @throws DocumentError
     */
    public void verify(
            final LinkedTree expanded,
            final LinkedTree unsignedProof,
            final byte[] verificationKey,
            final byte[] signature) throws VerificationError {

        Objects.requireNonNull(expanded);
        Objects.requireNonNull(unsignedProof);
        Objects.requireNonNull(verificationKey);
        Objects.requireNonNull(signature);

        try {
            final byte[] computeSignature = hashCode(expanded, unsignedProof);

            suite.verify(verificationKey, signature, computeSignature);

        } catch (CryptoSuiteError e) {
            throw new VerificationError(VerificationErrorCode.InvalidSignature, e);
        }
    }

    /**
     * Issues the given VC/VP document and returns the document signature.
     *
     * @see <A href=
     *      "https://w3c-ccg.github.io/data-integrity-spec/#proof-algorithm">Proof
     *      Algorithm</a>
     *
     * @param expanded   expanded unsigned VC/VP document
     * @param privateKey
     * @param proof
     *
     * @return computed signature
     *
     * @throws SigningError
     * @throws DocumentError
     */
    public byte[] sign(LinkedTree expanded, byte[] privateKey, LinkedTree proof) throws SigningError {

        Objects.requireNonNull(expanded);
        Objects.requireNonNull(privateKey);
        Objects.requireNonNull(proof);

        try {
            final byte[] documentHashCode = hashCode(expanded, proof);

            return suite.sign(privateKey, documentHashCode);

        } catch (CryptoSuiteError e) {
            throw new SigningError(SigningError.SignatureErrorCode.Internal, e);
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
     * @throws CryptoSuiteError
     */
    byte[] hashCode(LinkedTree document, LinkedTree proof) throws CryptoSuiteError {

        byte[] proofHash = suite.digest(suite.canonicalize(proof));

        byte[] documentHash = suite.digest(suite.canonicalize(document));

        // proof hash + document hash
        byte[] result = new byte[proofHash.length + documentHash.length];

        System.arraycopy(proofHash, 0, result, 0, proofHash.length);
        System.arraycopy(documentHash, 0, result, proofHash.length, documentHash.length);

        return result;
    }
}
