package com.apicatalog.vc.issuer;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.apicatalog.controller.method.GenericMethodUri;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.VerifiableMaterial;

public abstract class ProofDraft {

    protected final String type;
    
    protected final VerificationMethod method;

    protected URI id;
    protected Collection<URI> previousProof;

    protected final URI purpose;

    protected Instant created;
    protected Instant expires;

    protected ProofDraft(String type, VerificationMethod method, URI purpose) {
        this.method = method;
        this.type = type;
        this.id = null;
        this.previousProof = null;
        this.purpose = purpose;
    }

    protected ProofDraft(String type, URI verificatonUrl, URI purpose) {
        this.method = new GenericMethodUri(verificatonUrl);
        this.type = type;
        this.id = null;
        this.previousProof = null;
        this.purpose = purpose;
    }

    public abstract VerifiableMaterial unsigned(Collection<String> documentContext, DocumentLoader loader, URI base) throws DocumentError;

    protected abstract VerifiableMaterial sign(VerifiableMaterial proof, byte[] signature) throws DocumentError;

    public abstract void validate() throws DocumentError;

    public void id(URI id) {
        this.id = id;
    }

    public void previousProof(URI previousProof) {
        this.previousProof = previousProof != null ? List.of(previousProof) : null;
    }

    public void previousProof(Collection<URI> previousProof) {
        this.previousProof = previousProof;
    }

    public URI id() {
        return id;
    }

    public VerificationMethod method() {
        return method;
    }

    public Collection<URI> previousProof() {
        return previousProof;
    }

    public URI purpose() {
        return purpose;
    }

    public Instant created() {
        return created;
    }

    public Instant expires() {
        return expires;
    }

    public String type() {
        return type;
    }
}
