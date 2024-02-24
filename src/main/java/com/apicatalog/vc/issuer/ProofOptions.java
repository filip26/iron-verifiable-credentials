package com.apicatalog.vc.issuer;

import java.util.Collection;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.ModelVersion;

public interface ProofOptions {

    /**
     * The proof JSON-LD context URI(s) to compact the proof
     *
     * @param model a credential data model version
     * @return the proof JSON-LD context URI(s)
     */
    Collection<String> context(ModelVersion model);

    /**
     * A set of parameters required to independently verify the proof, such as an
     * identifier for a public/private key pair that would be used in the proof.
     * Mandatory
     *
     * @return {@link VerificationMethod} to verify the proof signature
     */
    VerificationMethod method();

    /**
     * Returns a {@link CryptoSuite} used to create and to verify the proof value.
     * 
     * @return {@link CryptoSuite} attached to the proof.
     */
    CryptoSuite cryptoSuite();
 
}
