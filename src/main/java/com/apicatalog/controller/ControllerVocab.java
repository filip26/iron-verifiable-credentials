package com.apicatalog.controller;

import com.apicatalog.ld.VocabTerm;

public class ControllerVocab {

    public static final String SECURITY_VOCAB = "https://w3id.org/security#";

    public static final VocabTerm CONTROLLER = VocabTerm.create("controller", SECURITY_VOCAB);

    public static final VocabTerm PUBLIC_KEY = VocabTerm.create("publicKeyMultibase", SECURITY_VOCAB);
    public static final VocabTerm PRIVATE_KEY = VocabTerm.create("secretKeyMultibase", SECURITY_VOCAB);

    public static final VocabTerm EXPIRATION = VocabTerm.create("expiration", SECURITY_VOCAB);
    public static final VocabTerm REVOKED = VocabTerm.create("revoked", SECURITY_VOCAB);

    protected ControllerVocab() {
        // protected
    }
}
