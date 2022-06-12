package com.apicatalog.ld.signature.ed25519;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.multicodec.Multicodec.Codec;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Ed25519KeyPair2020 extends Ed25519VerificationKey2020 implements KeyPair {

    protected static final String PRIVATE_KEY_MULTIBASE = "privateKeyMultibase";

    protected byte[] privateKey;

    public Ed25519KeyPair2020(final URI id) {
        super(id, "https://w3id.org/security#Ed25519KeyPair2020");
    }

    public static Ed25519KeyPair2020 from(JsonObject json) throws DataError {

        URI id =  JsonLdUtils.getId(json).orElse(null);

        // TODO check json object type!
        final Ed25519KeyPair2020 key = new Ed25519KeyPair2020(id);

        Ed25519VerificationKey2020.from(key, json);

        if (JsonLdUtils.hasPredicate(json, BASE + PRIVATE_KEY_MULTIBASE)) {
            key.privateKey = getKey(json, PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);
        }

        return key;
    }


    @Override
    public JsonObject toJson() {
        final JsonObjectBuilder builder = Json.createObjectBuilder();

        super.toJson(builder);

        setKey(builder, privateKey, PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);

        return builder.build();
    }

    @Override
    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }
}