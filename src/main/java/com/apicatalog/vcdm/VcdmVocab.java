package com.apicatalog.vcdm;

import com.apicatalog.ld.Term;

public final class VcdmVocab {

    public static final String CONTEXT_MODEL_V1 = "https://www.w3.org/2018/credentials/v1";
    
    public static final String CONTEXT_MODEL_V2 = "https://www.w3.org/ns/credentials/v2";
    
    public static final String CREDENTIALS_VOCAB = "https://www.w3.org/2018/credentials#";

    public static final String SECURITY_VOCAB = "https://w3id.org/security#";

    public static final Term CREDENTIAL_TYPE = Term.create("VerifiableCredential", CREDENTIALS_VOCAB);
    
    public static final Term PRESENTATION_TYPE = Term.create("VerifiablePresentation", CREDENTIALS_VOCAB);

    public static final Term STATUS = Term.create("credentialStatus", CREDENTIALS_VOCAB);

    public static final Term ISSUANCE_DATE = Term.create("issuanceDate", CREDENTIALS_VOCAB);

    public static final Term SUBJECT = Term.create( "credentialSubject", CREDENTIALS_VOCAB);
    
    public static final Term ISSUER = Term.create("issuer", CREDENTIALS_VOCAB);

    public static final Term VALID_FROM = Term.create("validFrom", CREDENTIALS_VOCAB);
    
    public static final Term VALID_UNTIL = Term.create("validUntil", CREDENTIALS_VOCAB);
    
    public static final Term ISSUED = Term.create("issued", CREDENTIALS_VOCAB);

    public static final Term EXPIRATION_DATE = Term.create("expirationDate", CREDENTIALS_VOCAB);

    public static final Term CREDENTIAL_SCHEMA = Term.create("credentialSchema", CREDENTIALS_VOCAB);
    
    public static final Term REFRESH_SERVICE = Term.create("refreshService", CREDENTIALS_VOCAB);
    
    public static final Term TERMS_OF_USE = Term.create("termsOfUse", CREDENTIALS_VOCAB);
    
    public static final Term EVIDENCE = Term.create("evidence", CREDENTIALS_VOCAB);

    public static final Term VERIFIABLE_CREDENTIALS = Term.create("verifiableCredential", CREDENTIALS_VOCAB);

    public static final Term HOLDER = Term.create("holder", CREDENTIALS_VOCAB);

    public static final Term PROOF = Term.create("proof", SECURITY_VOCAB);
    
    public static final Term MULTIBASE_TYPE = Term.create("multibase", SECURITY_VOCAB);

    private VcdmVocab() { /* protected */ }
}
