package com.apicatalog.ld.signature.jws.from_lib_v070;

import java.net.URI;

/**
 * Originally class {@link com.apicatalog.ld.signature.proof.VerificationMethod} (in library version 0.7.0)
 */
public class VerificationMethod implements com.apicatalog.ld.signature.proof.VerificationMethod {

    protected URI id;

    protected String type;

    protected URI controller;

    public VerificationMethod() {
        this(null);
    }

    public VerificationMethod(URI id) {
        this.id = id;
    }

    public URI id() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setController(URI controller) {
        this.controller = controller;
    }

    public URI controller() {
        return controller;
    }
}
//public class VerificationMethod {
//
//    protected URI id;
//
//    protected String type;
//
//    protected URI controller;
//
//    public VerificationMethod() {
//        this(null);
//    }
//
//    public VerificationMethod(URI id) {
//        this.id = id;
//    }
//
//    public URI getId() {
//        return id;
//    }
//
//    public void setId(URI id) {
//        this.id = id;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public void setController(URI controller) {
//        this.controller = controller;
//    }
//
//    public URI getController() {
//        return controller;
//    }
//}