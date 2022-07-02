package com.apicatalog.did;

import com.apicatalog.ld.signature.proof.VerificationMethod;

public final class DidDocumentBuilder {

    private final DidDocumentImpl document;

    protected DidDocumentBuilder() {
        this.document = new DidDocumentImpl();
    }

    public static DidDocumentBuilder create() {
        return new DidDocumentBuilder();
    }

    public DidDocumentBuilder id(Did did) {
        this.document.id = did;
        return this;
    }

    public DidDocumentBuilder add(VerificationMethod verificationMethod) {
        this.document.verificationMethod.add(verificationMethod);
        return this;
    }

    public DidDocument build() {
        return document;
    }
}
