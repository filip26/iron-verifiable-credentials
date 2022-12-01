package com.apicatalog.vc;

import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.schema.LdValueAdapter;
import com.apicatalog.ld.schema.adapter.LdObjectAdapter;
import com.apicatalog.ld.signature.method.VerificationMethod;

import jakarta.json.JsonValue;

public class VcSchema extends LdSchema {
    
    public VcSchema(LdObjectAdapter schema) {
        super(schema);
    }
    
    public static final LdProperty<byte[]> proofValue(LdTerm id, LdValueAdapter<JsonValue, byte[]> adapter) {
        return property(id, adapter, VcSchemaTag.ProofValue.name());
    }
    
    public static final LdProperty<VerificationMethod> verificationMethod(LdTerm id, LdValueAdapter<JsonValue, VerificationMethod> adapter) {
        return property(id, adapter, VcSchemaTag.VerificationMethod.name());
    }
    
    public static final LdObjectAdapter proof(LdProperty<?>... properties) {
        return object(properties);
    }

        
}
