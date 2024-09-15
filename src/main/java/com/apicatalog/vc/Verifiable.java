package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.vc.proof.Proof;

/**
 * Represents a common ancestor for verifiable data.
 * 
 * @since 0.9.0
 */
public interface Verifiable extends Linkable {

    URI id();

    Collection<String> type();

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
}
