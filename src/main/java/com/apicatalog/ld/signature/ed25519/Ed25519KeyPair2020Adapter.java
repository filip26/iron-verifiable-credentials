package com.apicatalog.ld.signature.ed25519;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.ld.DocumentError;
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
    public VerificationMethod deserialize(JsonObject object) throws DocumentError {

        final URI id =  JsonLdUtils.getId(object).orElse(null);

        URI controller = controllerFrom(object).orElse(null);
        
        String type = JsonLdUtils.getType(object).stream().findFirst().orElse(null);
        
        byte[] publicKey = publicKeyFrom(object).orElse(null);

        byte[] privateKey = null;
        
        if (JsonLdUtils.hasPredicate(object, Ed25519Signature2020.BASE + PRIVATE_KEY_MULTIBASE)) {
            privateKey = getKey(object, Ed25519Signature2020.BASE + PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);
        }

        return new Ed25519KeyPair2020(
                        id,
                        controller,
                        type,
                        publicKey,
                        privateKey
                        );
    }



    @Override
    public JsonObject serialize(VerificationMethod proof) {

        final JsonObjectBuilder builder = Json.createObjectBuilder(super.serialize(proof));

        if (proof instanceof KeyPair) {
            setKey(builder, ((KeyPair) proof).privateKey(), PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);
        }

        return builder.build();

    }
}