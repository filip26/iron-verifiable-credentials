package com.apicatalog.vc;

import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.adapter.ObjectAdapter;

@Deprecated
public class VcSchema extends LdSchema {

    public VcSchema(ObjectAdapter schema) {
        super(schema);
    }

//    public static final LdProperty<byte[]> proofValue(LdTerm id, LdValueAdapter<JsonValue, byte[]> adapter) {
//        return property(id, adapter, VcTag.ProofValue.name());
//    }
//
//    public static final LdProperty<VerificationMethod> verificationMethod(
//            LdTerm id, 
//            LdValueAdapter<JsonValue, VerificationMethod> adapter) {
//        
//        return property(id, adapter, VcTag.VerificationMethod.name());
//    }
//
//    public static final LdSchema proof(LdProperty<?>... properties) {
//        return new LdSchema(object(properties));
//    }

}
