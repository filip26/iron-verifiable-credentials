package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

/**
 * Represents a common ancestor for verifiable data.
 *  
 * @since 0.9.0
 *
 */
public class Verifiable {

    protected URI id;
    
    protected Collection<Proof> proofs;
    protected Collection<String> type;

    public URI getId() {
        return id;
    }
    
    public void setId(URI id) {
        this.id = id;
    }
    
    public Collection<String> getType() {
        return type;
    }
    
    public void setType(Collection<String> type) {
        this.type = type;
    }
    
    public Collection<Proof> getProofs() {
        return proofs;
    }
    
    public void setProofs(Collection<Proof> proofs) {
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
}
