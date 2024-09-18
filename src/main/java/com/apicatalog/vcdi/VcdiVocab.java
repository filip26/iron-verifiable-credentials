package com.apicatalog.vcdi;

import com.apicatalog.ld.Term;
import com.apicatalog.vcdm.VcdmVocab;

public final class VcdiVocab {

    public static Term TYPE = Term.create("DataIntegrityProof", VcdmVocab.SECURITY_VOCAB);
    
    public static Term CREATED = Term.create("created", "http://purl.org/dc/terms/");
    
    public static Term EXPIRES = Term.create("expiration", VcdmVocab.SECURITY_VOCAB);

    public static Term PURPOSE = Term.create("proofPurpose", VcdmVocab.SECURITY_VOCAB);
    public static Term VERIFICATION_METHOD = Term.create("verificationMethod", VcdmVocab.SECURITY_VOCAB);

    public static Term PROOF_VALUE = Term.create("proofValue", VcdmVocab.SECURITY_VOCAB);

    public static Term DOMAIN = Term.create("domain", VcdmVocab.SECURITY_VOCAB);
    public static Term CHALLENGE = Term.create("challenge", VcdmVocab.SECURITY_VOCAB);
    public static Term NONCE = Term.create("nonce", VcdmVocab.SECURITY_VOCAB);

    public static Term CRYPTO_SUITE = Term.create("cryptosuite", VcdmVocab.SECURITY_VOCAB);

    public static Term PREVIOUS_PROOF = Term.create("previousProof", VcdmVocab.SECURITY_VOCAB);
    
    public static Term ASSERTION = Term.create("assertionMethod", VcdmVocab.SECURITY_VOCAB);
    public static Term AUTHENTICATION = Term.create("authentication", VcdmVocab.SECURITY_VOCAB);
}
