package com.apicatalog.vc.integrity;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.schema.LdValueAdapter;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.method.VerificationMethod;

public class DataIntegrityKeysAdapter implements LdValueAdapter<LdObject, VerificationMethod> {

    @Override
    public VerificationMethod read(LdObject object) {

        URI id = object.value(LdTerm.ID);
        URI type = object.value(LdTerm.TYPE);
        URI controller = object.value(DataIntegritySchema.CONTROLLER);
        
        byte[] publicKey = object.value(DataIntegritySchema.MULTIBASE_PUB_KEY);
        byte[] privateKey = object.value(DataIntegritySchema.MULTIBASE_PRIV_KEY);
        
        return new DataIntegrityKeyPair(id, type, controller, publicKey, privateKey);
    }

    @Override
    public LdObject write(VerificationMethod method) {

        Map<String, Object> result = new LinkedHashMap<>();
        
        if (method.id() != null) {
            result.put(LdTerm.ID.id(), method.id());
        }
        if (method.type() != null) {
            result.put(LdTerm.TYPE.id(), method.type());
        }
        if (method.controller() != null) {
            result.put(DataIntegritySchema.CONTROLLER.id(), method.controller());
        }
        
        if (method instanceof VerificationKey) {
            VerificationKey key = (VerificationKey)method;
            
            if (key.publicKey() != null) {
                result.put(DataIntegritySchema.MULTIBASE_PUB_KEY.id(), key.publicKey());
            }
        }
        
        return new LdObject(result);
    }

    
    
    
    
}
