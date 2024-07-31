package com.apicatalog.vc;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.VerifiableObject;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.proof.Proof;

/**
 * Represents a common ancestor for verifiable data.
 * 
 * @since 0.9.0
 */
public interface Verifiable extends VerifiableObject {
        
    Collection<Proof> proofs();

    default boolean isCredential() {
        return false;
    }

    default boolean isPresentation() {
        return false;
    }

    default Credential asCredential() {
        throw new ClassCastException();
    }

    default Presentation asPresentation() {
        throw new ClassCastException();
    }

    void validate() throws DocumentError;

    /**
     * Verifiable credentials data model version.
     * 
     * @return the data model version, never <code>null</code>
     */
    ModelVersion version();
}
