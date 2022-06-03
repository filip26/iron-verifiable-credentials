package com.apicatalog.lds.ed25519;

import java.net.URI;

import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.DataIntegrityError.Code;
import com.apicatalog.lds.key.VerificationKey;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.multicodec.Multicodec.Type;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

//TODO javadoc
public class Ed2551VerificationKey2020 implements VerificationKey {

    protected static final String ID = "id";
    protected static final String TYPE = "type";
    protected static final String CONTROLLER = "controller";
    protected static final String PUBLIC_KEY_MULTIBASE = "publicKeyMultibase";

    protected final URI id;
    protected final String type;
    protected URI controller;

    protected byte[] publicKey;

    public Ed2551VerificationKey2020(final URI id) {
        this(id, "https://w3id.org/security#Ed25519VerificationKey2020");
    }

    protected Ed2551VerificationKey2020(final URI id, final String type) {
        this.id = id;
        this.type = type;
    }

    public static final Ed2551VerificationKey2020 reference(URI id) {
        return new Ed2551VerificationKey2020(id);
    }

    public static Ed2551VerificationKey2020 from(JsonObject json) throws DataIntegrityError {

        // TODO check json object type!
        final Ed2551VerificationKey2020 key =new Ed2551VerificationKey2020(URI.create(json.getString(ID)), "https://w3id.org/security#Ed25519VerifiableKey2020");

        return from(key, json);
    }

    protected static final  <T extends Ed2551VerificationKey2020> T from(T key, JsonObject json) throws DataIntegrityError {

        //TODO check type        key.type = json.getString(TYPE);

        key.controller = URI.create(json.getString(CONTROLLER));
        key.publicKey = getKey(json, PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);

        // verify verification key length - TODO needs to be clarified
        if (key.publicKey.length != 32 && key.publicKey.length != 57 && key.publicKey.length != 114) {
            throw new DataIntegrityError(Code.InvalidProofValueLength);
        }
        return key;
    }

    @Override
    public JsonObject toJson() {
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        toJson(builder);
        return builder.build();
    }

    protected void toJson(JsonObjectBuilder builder) {
        if (id != null) {
            builder.add(ID, id.toString());
        }
        builder.add(TYPE, type);

        if (controller != null) {
            builder.add(CONTROLLER, controller.toString());
        }

        setKey(builder, publicKey, PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);
    }

    public URI getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    public URI getController() {
        return controller;
    }

    public void setController(URI controller) {
        this.controller = controller;
    }

    @Override
    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
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

        final String multibase = Multibase.encode(Algorithm.Base58Btc, encoded);

        builder.add(property, multibase);
    }
}