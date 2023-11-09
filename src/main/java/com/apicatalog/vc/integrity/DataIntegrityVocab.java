package com.apicatalog.vc.integrity;

import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.vc.VcVocab;

public final class DataIntegrityVocab {

    public static final LdTerm CREATED = LdTerm.create("created", "http://purl.org/dc/terms/");
    
    public static final LdTerm PURPOSE = LdTerm.create("proofPurpose", VcVocab.SECURITY_VOCAB);
    public static final LdTerm VERIFICATION_METHOD = LdTerm.create("verificationMethod", VcVocab.SECURITY_VOCAB);
    
    public static final LdTerm PROOF_VALUE = LdTerm.create("proofValue", VcVocab.SECURITY_VOCAB);
    
    public static final LdTerm DOMAIN = LdTerm.create("domain", VcVocab.SECURITY_VOCAB);
    public static final LdTerm CHALLENGE = LdTerm.create("challenge", VcVocab.SECURITY_VOCAB);

    public static final LdTerm CRYPTO_SUITE = LdTerm.create("cryptosuite", VcVocab.SECURITY_VOCAB);

    public static final LdTerm PREVIOUS_PROOF = LdTerm.create("previousProof", VcVocab.SECURITY_VOCAB);

    public static final LdTerm CONTROLLER = LdTerm.create("controller", VcVocab.SECURITY_VOCAB);
    
    public static final LdTerm MULTIBASE_PUB_KEY = LdTerm.create("publicKeyMultibase", VcVocab.SECURITY_VOCAB);
    public static final LdTerm MULTIBASE_PRIV_KEY = LdTerm.create("privateKeyMultibase", VcVocab.SECURITY_VOCAB);

    private DataIntegrityVocab() {
        /* protected */ }
}
