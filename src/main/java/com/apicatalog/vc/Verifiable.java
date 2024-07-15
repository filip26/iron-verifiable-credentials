package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.proof.Proof;

/**
 * Represents a common ancestor for verifiable data.
 * 
 * @since 0.9.0
 */
public abstract class Verifiable {

    protected final ModelVersion version;

    protected URI id;

    protected Collection<Proof> proofs;
    protected Collection<String> type;

    protected Verifiable(ModelVersion version) {
        this.version = version;
    }

    public URI id() {
        return id;
    }

    public Collection<String> type() {
        return type;
    }

    public Collection<Proof> proofs() {
        return proofs;
    }

    public void proofs(Collection<Proof> proofs) {
        this.proofs = proofs;
    }

    public boolean isCredential() {
        return false;
    }

    public boolean isPresentation() {
        return false;
    }

    public Credential asCredential() {
        throw new ClassCastException();
    }

    public Presentation asPresentation() {
        throw new ClassCastException();
    }

    /**
     * Verifiable credentials data model version.
     * 
     * @return the data model version, never <code>null</code>
     */
    public ModelVersion version() {
        return version;
    }

    public abstract void validate() throws DocumentError;
}
