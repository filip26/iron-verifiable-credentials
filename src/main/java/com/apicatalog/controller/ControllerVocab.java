package com.apicatalog.controller;

import com.apicatalog.ld.Term;

public class ControllerVocab {

    public static final String SECURITY_VOCAB = "https://w3id.org/security#";

    public static final Term CONTROLLER = Term.create("controller", SECURITY_VOCAB);

    public static final Term PUBLIC_KEY = Term.create("publicKeyMultibase", SECURITY_VOCAB);
    public static final Term PRIVATE_KEY = Term.create("secretKeyMultibase", SECURITY_VOCAB);

    public static final Term EXPIRATION = Term.create("expiration", SECURITY_VOCAB);
    public static final Term REVOKED = Term.create("revoked", SECURITY_VOCAB);

    protected ControllerVocab() {
        // protected
    }
}
