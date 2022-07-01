package com.apicatalog.ld.signature.ed25519;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.apicatalog.multicodec.Multicodec.Codec;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Ed25519KeyPair2020Adapter extends Ed25519VerificationKey2020Adapter {

    protected static final String TYPE = "Ed25519KeyPair2020";
    protected static final String PRIVATE_KEY_MULTIBASE = "privateKeyMultibase";

    @Override
    public String getType() {
	return Ed25519Signature2020.BASE + TYPE;
    }
    
    @Override
    public VerificationMethod deserialize(JsonObject object) throws DataError {
	
        URI id =  JsonLdUtils.getId(object).orElse(null);

        final KeyPair key = new KeyPair(id);

        Ed25519VerificationKey2020Adapter.from(key, object);

        if (JsonLdUtils.hasPredicate(object, Ed25519Signature2020.BASE + PRIVATE_KEY_MULTIBASE)) {
            key.setPrivateKey(getKey(object, Ed25519Signature2020.BASE + PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey));
        }

        return key;
    }



    @Override
    public JsonObject serialize(VerificationMethod proof) {

        final JsonObjectBuilder builder = Json.createObjectBuilder(super.serialize(proof));

        if (proof instanceof KeyPair) {
            setKey(builder, ((KeyPair) proof).getPrivateKey(), PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);
        }

        return builder.build();

    }    
}