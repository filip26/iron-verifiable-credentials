package com.apicatalog.vc;

import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.schema.LdValueAdapter;
import com.apicatalog.ld.schema.adapter.LdObjectAdapter;
import com.apicatalog.ld.signature.method.VerificationMethod;

import jakarta.json.JsonValue;

public class VcSchema extends LdSchema {
    
    public static final String CRED_VOCAB = "https://www.w3.org/2018/credentials#";
    public static final String SEC_VOCAB = "https://w3id.org/security#";

    public static final LdTerm STATUS = LdTerm.create("credentialStatus", CRED_VOCAB);
    
    public static final LdTerm ISSUANCE_DATE = LdTerm.create("issuanceDate", CRED_VOCAB);
    
    public static final LdTerm VERIFIABLE_CREDENTIALS = LdTerm.create("verifiableCredential", CRED_VOCAB);
    
    public static final LdTerm HOLDER = LdTerm.create("holder", CRED_VOCAB);

    public static final LdTerm PROOF = LdTerm.create("proof", SEC_VOCAB);
    
    public VcSchema(LdObjectAdapter schema) {
        super(schema);
    }
    
    public static final LdProperty<byte[]> proofValue(LdTerm id, LdValueAdapter<JsonValue, byte[]> adapter) {
        return property(id, adapter, VcSchemaTag.ProofValue.name());
    }
    
    public static final LdProperty<VerificationMethod> verificationMethod(LdTerm id, LdValueAdapter<JsonValue, VerificationMethod> adapter) {
        return property(id, adapter, VcSchemaTag.VerificationMethod.name());
    }
    
    public static final LdSchema proof(LdProperty<?>... properties) {
        return new LdSchema(object(properties));
    }

        
}
