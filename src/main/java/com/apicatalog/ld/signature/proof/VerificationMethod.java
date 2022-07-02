package com.apicatalog.ld.signature.proof;

import java.net.URI;

/**
 * Represents proof verification method declaration.
 *
 * @see {@link <a href="https://w3c-ccg.github.io/data-integrity-spec/#verification-methods">Verification Methods</a>}
 *
 */
public class VerificationMethod {

    protected URI id;

    protected String type;

    protected URI controller;

    public VerificationMethod() {
    this(null);
    }

    public VerificationMethod(URI id) {
    this.id = id;
    }

    public URI getId() {
    return id;
    }

    public void setId(URI id) {
    this.id = id;
    }

    public String getType() {
    return type;
    }

    public void setType(String type) {
    this.type = type;
    }

    public void setController(URI controller) {
    this.controller = controller;
    }

    public URI getController() {
    return controller;
    }
}
