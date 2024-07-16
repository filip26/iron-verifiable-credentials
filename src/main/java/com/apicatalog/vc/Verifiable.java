package com.apicatalog.vc;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonObject;

/**
 * Represents a common ancestor for verifiable data.
 * 
 * @since 0.9.0
 */
public abstract class Verifiable extends DataObject {

    protected Collection<Proof> proofs;

    protected Verifiable(ModelVersion version, JsonObject expanded) {
        super(version, expanded);
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

    public abstract void validate() throws DocumentError;
}
