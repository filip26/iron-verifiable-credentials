package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.schema.LdValueAdapter;
import com.apicatalog.ld.signature.method.VerificationMethod;

public class DataIntegrityKeysAdapter implements LdValueAdapter<LdObject, VerificationMethod> {

    @Override
    public VerificationMethod read(LdObject object) {

        URI id = object.value(LdTerm.ID);
        URI type = object.value(LdTerm.TYPE);
        
        byte[] publicKey = object.value(DataIntegritySchema.MULTIBASE_PUB_KEY);
        byte[] privateKey = object.value(DataIntegritySchema.MULTIBASE_PRIV_KEY);
        
        return new DataIntegrityKeyPair(id, type, null, publicKey, privateKey);
    }

    @Override
    public LdObject write(VerificationMethod value) {
        // TODO Auto-generated method stub
        return null;
    }

    
    
    
    
}
