package com.apicatalog.lds.ed25519;

import com.apicatalog.code.Multicodec;
import com.apicatalog.code.Multicodec.Codec;
import com.apicatalog.code.Multicodec.Type;
import com.apicatalog.lds.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.DataIntegrityError;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Ed25519KeyPair2020 implements KeyPair {

    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String CONTROLLER = "controller";
    private static final String PUBLIC_KEY_MULTIBASE = "publicKeyMultibase";
    private static final String PRIVATE_KEY_MULTIBASE = "privateKeyMultibase";

    private String id;
    private String type;
    private String controller;

    private byte[] publicKey;
    private byte[] privateKey;

    public static final Ed25519KeyPair2020 from(JsonObject json) throws DataIntegrityError {

        final Ed25519KeyPair2020 key = new Ed25519KeyPair2020();

        // TODO check json object type!

        key.id = json.getString(ID);
        key.type = json.getString(TYPE);
        key.controller = json.getString(CONTROLLER);
        key.publicKey = getKey(json, PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);
        
        // verify verification key length - TODO needs to be clarified
        if (key.publicKey.length == 32 || key.publicKey.length == 57 || key.publicKey.length == 114) {
            //FIXME throw new VerificationError(Code.InvalidProofLength);
        }
        
        key.privateKey = getKey(json, PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);

        return key;
    }

    @Override
    public JsonObject toJson() {
        final JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add(ID, id);
        builder.add(TYPE, type);

        if (controller != null) {
            builder.add(CONTROLLER, controller);
        }

        setKey(builder, publicKey, PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);
        setKey(builder, privateKey, PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);

        return builder.build();
    }

    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getController() {
        return controller;
    }

    @Override
    public byte[] getPrivateKey() {
        return privateKey;
    }

    @Override
    public byte[] getPublicKey() {
        return publicKey;
    }

    static byte[] getKey(JsonObject json, String property, Codec expected) throws DataIntegrityError {

        if (!json.containsKey(property)) {
            return null;
        }

        final String privateKeyMultibase = json.getString(property);

        // decode private key
        final byte[] encodedKey = Multibase.decode(privateKeyMultibase);
        
        final Codec codec = Multicodec.codec(Type.Key, encodedKey).orElseThrow(DataIntegrityError::new);

        if (expected != codec) {
            throw new DataIntegrityError();
        }

        return Multicodec.decode(codec, encodedKey);
    }

    static void setKey(JsonObjectBuilder builder, byte[] key, String property, Codec codec) {

        if (key == null || key.length == 0) {
            return;
        }

        final byte[] encoded = Multicodec.encode(codec, key);

        final String multibase = Multibase.encode(encoded);

        builder.add(property, multibase);
    }
}