package com.apicatalog.ld.signature.proof;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.signature.method.VerificationMethod;

public interface UnsignedProof {

//    /**
//     * The proof type used.
//     *
//     * For example, an Ed25519Signature2020 type indicates that the proof includes
//     * a digital signature produced by an ed25519 cryptographic key.
//     *
//     * @return the proof type
//     */
//    String getType();

    /**
     * The intent for the proof, the reason why an entity created it.
     * e.g. assertion or authentication
     *
     * @see <a href="https://w3c-ccg.github.io/data-integrity-spec/#proof-purposes">Proof Purposes</a>
     *
     * @return {@link URI} identifying the purpose
     */
    URI getPurpose();

    /**
     * A set of parameters required to independently verify the proof,
     * such as an identifier for a public/private key pair that would be used in the proof.
     *
     * @return {@link VerificationMethod} to verify the proof signature
     */
    VerificationMethod getMethod();

    /**
     * The string value of an ISO8601.
     *
     * @return the date time when the proof has been created
     */
    Instant getCreated();

    /**
     * A string value specifying the restricted domain of the proof.
     *
     * @return the domain
     */
    String getDomain();
}
