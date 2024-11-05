package com.apicatalog.vc.di;

import com.apicatalog.ld.VocabTerm;
import com.apicatalog.vcdm.VcdmVocab;

public final class VcdiVocab {

    public static VocabTerm TYPE = VocabTerm.create("DataIntegrityProof", VcdmVocab.SECURITY_VOCAB);
    
    public static VocabTerm CREATED = VocabTerm.create("created", "http://purl.org/dc/terms/");
    
    public static VocabTerm EXPIRES = VocabTerm.create("expiration", VcdmVocab.SECURITY_VOCAB);

    public static VocabTerm PURPOSE = VocabTerm.create("proofPurpose", VcdmVocab.SECURITY_VOCAB);
    public static VocabTerm VERIFICATION_METHOD = VocabTerm.create("verificationMethod", VcdmVocab.SECURITY_VOCAB);

    public static VocabTerm PROOF_VALUE = VocabTerm.create("proofValue", VcdmVocab.SECURITY_VOCAB);

    public static VocabTerm DOMAIN = VocabTerm.create("domain", VcdmVocab.SECURITY_VOCAB);
    public static VocabTerm CHALLENGE = VocabTerm.create("challenge", VcdmVocab.SECURITY_VOCAB);
    public static VocabTerm NONCE = VocabTerm.create("nonce", VcdmVocab.SECURITY_VOCAB);

    public static VocabTerm CRYPTO_SUITE = VocabTerm.create("cryptosuite", VcdmVocab.SECURITY_VOCAB);

    public static VocabTerm PREVIOUS_PROOF = VocabTerm.create("previousProof", VcdmVocab.SECURITY_VOCAB);
    
    public static VocabTerm ASSERTION = VocabTerm.create("assertionMethod", VcdmVocab.SECURITY_VOCAB);
    public static VocabTerm AUTHENTICATION = VocabTerm.create("authentication", VcdmVocab.SECURITY_VOCAB);
}
