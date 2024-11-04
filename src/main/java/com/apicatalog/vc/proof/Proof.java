package com.apicatalog.vc.proof;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Type;
import com.apicatalog.vc.di.DeprecatedDataIntegrityProof;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

/**
 * Represents generic VC/VP proof.
 *
 * @see {@link DeprecatedDataIntegrityProof} for an example implementation
 */
@Fragment(generic = true)
public interface Proof extends Linkable {

    /**
     * A set of parameters required to independently verify the proof, such as an
     * identifier for a public/private key pair that would be used in the proof.
     * Mandatory
     *
     * @return {@link VerificationMethod} to verify the proof signature
     */
    @Term("verificationMethod")
    VerificationMethod method();

    /**
     * One of any number of valid representations of proof value generated by the
     * Proof Algorithm.
     *
     * @return the proof value
     */
    @Term("proofValue")
    ProofValue signature();

    /**
     * The proof unique identifier. Optional.
     * 
     * @return {@link URI} representing the proof id
     */
    @Id
    URI id();

    @Type
    Collection<String> type();

    /**
     * Must be processed after the previous proof. Allow to create a chain of
     * proofs. Optional.
     * 
     * @return {@link URI} uniquely identifying the previous proof
     */
    @Term
    URI previousProof();

    /**
     * Returns a {@link CryptoSuite} used to create and to verify the proof value.
     * 
     * @return {@link CryptoSuite} attached to the proof.
     */
    CryptoSuite cryptoSuite();

    /**
     * The intent for the proof, the reason why an entity created it. e.g.
     * an assertion, authentication.
     *
     * @return {@link URI} identifying the purpose
     */
    @Term("proofPurpose")
    URI purpose();

    /**
     * Validates the proof data, not a signature.
     * 
     * @param params custom, suite specific parameters to validate against
     * 
     * @throws DocumentError if the proof is not valid, e.g. is created in the
     *                       future
     */
    void validate(Map<String, Object> params) throws DocumentError;

    void verify(VerificationKey method) throws VerificationError, DocumentError;

    default JsonObject derive(JsonStructure context, JsonObject data, Collection<String> selectors) throws SigningError, DocumentError {
        throw new UnsupportedOperationException("The proof does not support a selective disclosure.");
    }
}
