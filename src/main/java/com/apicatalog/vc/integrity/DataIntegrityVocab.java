package com.apicatalog.vc.integrity;

import com.apicatalog.ld.Term;
import com.apicatalog.vcdm.VcdmVocab;

public final class DataIntegrityVocab {

    public static final Term CREATED = Term.create("created", "http://purl.org/dc/terms/");

    public static final Term PURPOSE = Term.create("proofPurpose", VcdmVocab.SECURITY_VOCAB);
    public static final Term VERIFICATION_METHOD = Term.create("verificationMethod", VcdmVocab.SECURITY_VOCAB);

    public static final Term PROOF_VALUE = Term.create("proofValue", VcdmVocab.SECURITY_VOCAB);

    public static final Term DOMAIN = Term.create("domain", VcdmVocab.SECURITY_VOCAB);
    public static final Term CHALLENGE = Term.create("challenge", VcdmVocab.SECURITY_VOCAB);
    public static final Term NONCE = Term.create("nonce", VcdmVocab.SECURITY_VOCAB);

    public static final Term CRYPTO_SUITE = Term.create("cryptosuite", VcdmVocab.SECURITY_VOCAB);

    public static final Term PREVIOUS_PROOF = Term.create("previousProof", VcdmVocab.SECURITY_VOCAB);

    private DataIntegrityVocab() {
        /* protected */ }
}
