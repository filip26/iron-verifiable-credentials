package com.apicatalog.vc;

import com.apicatalog.jsonld.schema.LdTerm;

public final class VcVocab {

    public static final String CREDENTIALS_VOCAB = "https://www.w3.org/2018/credentials#";

    public static final String SECURITY_VOCAB = "https://w3id.org/security#";

    public static final LdTerm CREDENTIAL_TYPE = LdTerm.create("VerifiableCredential", CREDENTIALS_VOCAB);
    
    public static final LdTerm PRESENTATION_TYPE = LdTerm.create("VerifiablePresentation", CREDENTIALS_VOCAB);

    public static final LdTerm STATUS = LdTerm.create("credentialStatus", CREDENTIALS_VOCAB);

    public static final LdTerm ISSUANCE_DATE = LdTerm.create("issuanceDate", CREDENTIALS_VOCAB);

    public static final LdTerm SUBJECT = LdTerm.create( "credentialSubject", CREDENTIALS_VOCAB);
    
    public static final LdTerm ISSUER = LdTerm.create("issuer", CREDENTIALS_VOCAB);

    public static final LdTerm VALID_FROM = LdTerm.create("validFrom", CREDENTIALS_VOCAB);
    
    public static final LdTerm VALID_UNTIL = LdTerm.create("validUntil", CREDENTIALS_VOCAB);
    
    public static final LdTerm ISSUED = LdTerm.create("issued", CREDENTIALS_VOCAB);

    public static final LdTerm EXPIRATION_DATE = LdTerm.create("expirationDate", CREDENTIALS_VOCAB);

    public static final LdTerm CREDENTIAL_SCHEMA = LdTerm.create("credentialSchema", CREDENTIALS_VOCAB);
    
    public static final LdTerm REFRESH_SERVICE = LdTerm.create("refreshService", CREDENTIALS_VOCAB);
    
    public static final LdTerm TERMS_OF_USE = LdTerm.create("termsOfUse", CREDENTIALS_VOCAB);
    
    public static final LdTerm EVIDENCE = LdTerm.create("evidence", CREDENTIALS_VOCAB);

    public static final LdTerm VERIFIABLE_CREDENTIALS = LdTerm.create("verifiableCredential", CREDENTIALS_VOCAB);

    public static final LdTerm HOLDER = LdTerm.create("holder", CREDENTIALS_VOCAB);

    public static final LdTerm PROOF = LdTerm.create("proof", SECURITY_VOCAB);
    
    private VcVocab() { /* protected */ }
}
