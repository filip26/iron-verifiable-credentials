package com.apicatalog.vc.integrity;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.jsonld.schema.LdObject;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.jsonld.schema.adapter.LdValueAdapter;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;

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

        final Map<String, Object> result = new LinkedHashMap<>();

        if (method.id() != null) {
            result.put(LdTerm.ID.uri(), method.id());
        }
        if (method.type() != null) {
            result.put(LdTerm.TYPE.uri(), method.type());
        }
        if (method.controller() != null) {
            result.put(DataIntegritySchema.CONTROLLER.uri(), method.controller());
        }

        if (method instanceof VerificationKey) {
            VerificationKey key = (VerificationKey) method;

            if (key.publicKey() != null) {
                result.put(DataIntegritySchema.MULTIBASE_PUB_KEY.uri(), key.publicKey());
            }
        }

        return new LdObject(result);
    }
}
