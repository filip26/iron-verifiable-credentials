package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Type;
import com.apicatalog.vc.proof.Proof;

/**
 * Represents a common ancestor for verifiable data/material.
 */
@Fragment(generic = true)
public interface Verifiable {

    @Id
    URI id();

    @Type
    Collection<String> type();

    @Term
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
