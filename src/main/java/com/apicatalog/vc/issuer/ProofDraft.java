package com.apicatalog.vc.issuer;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import com.apicatalog.controller.method.GenericMethodUri;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.VerifiableMaterial;

public abstract class ProofDraft {

    protected final VerificationMethod method;

    protected URI id;
    protected Collection<URI> previousProof;

    protected ProofDraft(VerificationMethod method) {
        this.method = method;
        this.id = null;
        this.previousProof = null;
    }

    protected ProofDraft(URI verificatonUrl) {
        this.method = new GenericMethodUri(verificatonUrl);
        this.id = null;
        this.previousProof = null;
    }

    public abstract VerifiableMaterial unsigned(DocumentLoader loader, URI base) throws DocumentError;

    protected abstract VerifiableMaterial sign(VerifiableMaterial proof, byte[] signature) throws DocumentError;

    public abstract void validate() throws DocumentError;

    public void id(URI id) {
        this.id = id;
    }

    public void previousProof(URI previousProof) {
        this.previousProof = previousProof != null ? List.of(previousProof) : null;
    }
    
    public void previousProof(URI... previousProof) {
        this.previousProof = List.of(previousProof);
    }

}
