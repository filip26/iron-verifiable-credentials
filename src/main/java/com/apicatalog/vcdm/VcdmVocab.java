package com.apicatalog.vcdm;

import com.apicatalog.ld.VocabTerm;

public final class VcdmVocab {

    public static final String CONTEXT_MODEL_V1 = "https://www.w3.org/2018/credentials/v1";
    
    public static final String CONTEXT_MODEL_V2 = "https://www.w3.org/ns/credentials/v2";
    
    public static final String CREDENTIALS_VOCAB = "https://www.w3.org/2018/credentials#";

    public static final String SECURITY_VOCAB = "https://w3id.org/security#";

    public static final VocabTerm CREDENTIAL_TYPE = VocabTerm.create("VerifiableCredential", CREDENTIALS_VOCAB);
    
    public static final VocabTerm PRESENTATION_TYPE = VocabTerm.create("VerifiablePresentation", CREDENTIALS_VOCAB);

    public static final VocabTerm STATUS = VocabTerm.create("credentialStatus", CREDENTIALS_VOCAB);

    public static final VocabTerm ISSUANCE_DATE = VocabTerm.create("issuanceDate", CREDENTIALS_VOCAB);

    public static final VocabTerm SUBJECT = VocabTerm.create( "credentialSubject", CREDENTIALS_VOCAB);
    
    public static final VocabTerm ISSUER = VocabTerm.create("issuer", CREDENTIALS_VOCAB);

    public static final VocabTerm VALID_FROM = VocabTerm.create("validFrom", CREDENTIALS_VOCAB);
    
    public static final VocabTerm VALID_UNTIL = VocabTerm.create("validUntil", CREDENTIALS_VOCAB);
    
    public static final VocabTerm ISSUED = VocabTerm.create("issued", CREDENTIALS_VOCAB);

    public static final VocabTerm EXPIRATION_DATE = VocabTerm.create("expirationDate", CREDENTIALS_VOCAB);

    public static final VocabTerm CREDENTIAL_SCHEMA = VocabTerm.create("credentialSchema", CREDENTIALS_VOCAB);
    
    public static final VocabTerm REFRESH_SERVICE = VocabTerm.create("refreshService", CREDENTIALS_VOCAB);
    
    public static final VocabTerm TERMS_OF_USE = VocabTerm.create("termsOfUse", CREDENTIALS_VOCAB);
    
    public static final VocabTerm EVIDENCE = VocabTerm.create("evidence", CREDENTIALS_VOCAB);

    public static final VocabTerm VERIFIABLE_CREDENTIALS = VocabTerm.create("verifiableCredential", CREDENTIALS_VOCAB);

    public static final VocabTerm HOLDER = VocabTerm.create("holder", CREDENTIALS_VOCAB);

    public static final VocabTerm PROOF = VocabTerm.create("proof", SECURITY_VOCAB);
    
    public static final VocabTerm MULTIBASE_TYPE = VocabTerm.create("multibase", SECURITY_VOCAB);

    public static final VocabTerm ENVELOPED_CREDENTIAL_TYPE = VocabTerm.create("EnvelopedVerifiableCredential", CREDENTIALS_VOCAB);
    
    public static final VocabTerm ENVELOPED_PRESENTATION_TYPE = VocabTerm.create("EnvelopedVerifiablePresentation", CREDENTIALS_VOCAB);

    public static final VocabTerm NAME = VocabTerm.create("name", "https://schema.org/");
    public static final VocabTerm DESCRIPTION = VocabTerm.create("description", "https://schema.org/");
    
    private VcdmVocab() { /* protected */ }
}
