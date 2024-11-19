package com.apicatalog.cryptosuite;

import java.util.Objects;

import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.VerifiableMaterial;

public class Signature {

    protected final CryptoSuite suite;
    protected byte[] signature;

    public Signature(CryptoSuite suite, byte[] signature) {
        this.suite = suite;
        this.signature = signature;
    }

    /**
     * Verifies the given signed VC/VP document.
     *
     * @see <a href=
     *      "https://w3c-ccg.github.io/data-integrity-spec/#proof-verification-algorithm">Verification
     *      Algorithm</a>
     *
     * @param data            expanded unsigned VC/VP document
     * @param unsignedProof   expanded proof with no proofValue
     * @param verificationKey
     *
     * @throws VerificationError
     * @throws DocumentError
     */
    public void verify(
            final VerifiableMaterial data,
            final VerifiableMaterial unsignedProof,
            final byte[] verificationKey) throws VerificationError {

        Objects.requireNonNull(data);
        Objects.requireNonNull(unsignedProof);
        Objects.requireNonNull(verificationKey);

        try {
            final byte[] computeSignature = hashCode(data, unsignedProof);

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
     * @param verifiable an unsigned VC/VP document
     * @param proof      a proof with no signature, proofValue, attached *
     * @param privateKey a private key to sign with
     *
     * @return a signature
     * @throws CryptoSuiteError
     *
     * @throws DocumentError
     */
    public byte[] sign(VerifiableMaterial verifiable, VerifiableMaterial proof, byte[] privateKey) throws CryptoSuiteError {

        Objects.requireNonNull(verifiable);
        Objects.requireNonNull(proof);
        Objects.requireNonNull(privateKey);

        final byte[] documentHashCode = hashCode(verifiable, proof);

        signature = suite.sign(privateKey, documentHashCode);
        return signature;
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
    byte[] hashCode(VerifiableMaterial document, VerifiableMaterial proof) throws CryptoSuiteError {

        byte[] proofHash = suite.digest(suite.canonicalize(proof));

        byte[] documentHash = suite.digest(suite.canonicalize(document));

        // proof hash + document hash
        byte[] result = new byte[proofHash.length + documentHash.length];

        System.arraycopy(proofHash, 0, result, 0, proofHash.length);
        System.arraycopy(documentHash, 0, result, proofHash.length, documentHash.length);

        return result;
    }

    public byte[] value() {
        return signature;
    }

    public CryptoSuite suite() {
        return suite;
    }
}
