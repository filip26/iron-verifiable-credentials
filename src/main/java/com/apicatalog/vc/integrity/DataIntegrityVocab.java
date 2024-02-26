package com.apicatalog.vc.integrity;

import com.apicatalog.ld.Term;
import com.apicatalog.vc.VcVocab;

public final class DataIntegrityVocab {

    public static final Term CREATED = Term.create("created", "http://purl.org/dc/terms/");

    public static final Term PURPOSE = Term.create("proofPurpose", VcVocab.SECURITY_VOCAB);
    public static final Term VERIFICATION_METHOD = Term.create("verificationMethod", VcVocab.SECURITY_VOCAB);

    public static final Term PROOF_VALUE = Term.create("proofValue", VcVocab.SECURITY_VOCAB);

    public static final Term DOMAIN = Term.create("domain", VcVocab.SECURITY_VOCAB);
    public static final Term CHALLENGE = Term.create("challenge", VcVocab.SECURITY_VOCAB);
    public static final Term NONCE = Term.create("nonce", VcVocab.SECURITY_VOCAB);

    public static final Term CRYPTO_SUITE = Term.create("cryptosuite", VcVocab.SECURITY_VOCAB);

    public static final Term PREVIOUS_PROOF = Term.create("previousProof", VcVocab.SECURITY_VOCAB);

    private DataIntegrityVocab() {
        /* protected */ }
}
