package com.apicatalog.vc;

import com.apicatalog.vc.method.VerificationMethod;
import com.apicatalog.jsonld.schema.LdProperty;
import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.jsonld.schema.adapter.LdValueAdapter;
import com.apicatalog.jsonld.schema.adapter.ObjectAdapter;

import jakarta.json.JsonValue;

public class VcSchema extends LdSchema {

    public VcSchema(ObjectAdapter schema) {
        super(schema);
    }

    public static final LdProperty<byte[]> proofValue(LdTerm id, LdValueAdapter<JsonValue, byte[]> adapter) {
        return property(id, adapter, VcTag.ProofValue.name());
    }

    public static final LdProperty<VerificationMethod> verificationMethod(
            LdTerm id, 
            LdValueAdapter<JsonValue, VerificationMethod> adapter) {
        
        return property(id, adapter, VcTag.VerificationMethod.name());
    }

    public static final LdSchema proof(LdProperty<?>... properties) {
        return new LdSchema(object(properties));
    }

}
