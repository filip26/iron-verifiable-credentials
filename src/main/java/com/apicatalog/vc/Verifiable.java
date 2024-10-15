package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofProvider;

/**
 * Represents a common ancestor for verifiable data.
 * 
 * @since 0.9.0
 */
@Fragment(generic = true)
public interface Verifiable extends Linkable {

    @Id
    URI id();

    Collection<String> type();

    @Provided(ProofProvider.class)
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
