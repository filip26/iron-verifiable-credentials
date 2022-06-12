package com.apicatalog.ld.signature.ed25519;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.multicodec.Multicodec.Type;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

//TODO javadoc
public class Ed25519VerificationKey2020 implements VerificationKey {

    public static final String BASE = "https://w3id.org/security#";

    public static final String CONTROLLER = "controller";
    public static final String PUBLIC_KEY_MULTIBASE = "publicKeyMultibase";
    public static final String PUBLIC_KEY_TYPE = "https://w3id.org/security#multibase";

    protected final URI id;
    protected final String type;
    protected URI controller;

    protected byte[] publicKey;

    public Ed25519VerificationKey2020(final URI id) {
        this(id, "https://w3id.org/security#Ed25519VerificationKey2020");
    }

    protected Ed25519VerificationKey2020(final URI id, final String type) {
        this.id = id;
        this.type = type;
    }

    public static Ed25519VerificationKey2020 from(JsonObject json) throws DataError {

        // TODO check json object type!

        URI id =  JsonLdUtils.getId(json).orElse(null);

        final Ed25519VerificationKey2020 key = new Ed25519VerificationKey2020(id);

        return from(key, json);
    }

    protected static final <T extends Ed25519VerificationKey2020> T from(T key, JsonObject json) throws DataError {

        // TODO check type key.type = json.getString(TYPE);

        // controller
        JsonLdUtils.getObjects(json, BASE + CONTROLLER)
            .stream()
            .findFirst()
            .ifPresent(controller -> {

                if (JsonUtils.isArray(controller)) {
                    controller = controller.asJsonArray().get(0);
                }

                JsonLdUtils.getId(controller)
                    .ifPresent(id -> {
                        key.controller = id;
                    });
            });

        // public key
        if (JsonLdUtils.hasPredicate(json, BASE + PUBLIC_KEY_MULTIBASE)) {
            key.publicKey = getKey(json, PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);

            // verify verification key length - TODO needs to be clarified
            if (key.publicKey != null
                    && key.publicKey.length != 32
                    && key.publicKey.length != 57
                    && key.publicKey.length != 114
                    ) {
                throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE, "length");
            }
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
            builder.add(Keywords.ID, id.toString());
        }
        builder.add(Keywords.TYPE, type);

        if (controller != null) {
            builder.add(BASE + CONTROLLER,
                    Json.createArrayBuilder().add(Json.createObjectBuilder().add(Keywords.ID,
                    controller.toString())));
        }

        setKey(builder, publicKey, BASE + PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);
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

    static byte[] getKey(JsonObject json, String property, Codec expected) throws DataError {
        
        JsonValue key = JsonLdUtils
                            .getObjects(json, BASE + property)
                            .stream()
                            .findFirst()
                            .orElseThrow(DataError::new);    //FIXME

        if (JsonUtils.isArray(key)) {
            key = key.asJsonArray().get(0);
        }

        if (!ValueObject.isValueObject(key)) {
            throw new DataError();
        }

        if (!JsonLdUtils.isTypeOf(PUBLIC_KEY_TYPE, key.asJsonObject())) {
            throw new DataError();
        }

        final String privateKeyMultibase = ValueObject.getValue(key)
                .filter(JsonUtils::isString)
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .orElseThrow(DataError::new);

        // decode private key
        final byte[] encodedKey = Multibase.decode(privateKeyMultibase);

        final Codec codec = Multicodec.codec(Type.Key, encodedKey).orElseThrow(DataError::new);

        if (expected != codec) {
            throw new DataError();
        }

        return Multicodec.decode(codec, encodedKey);
    }

    static void setKey(JsonObjectBuilder builder, byte[] key, String property, Codec codec) {

        if (key == null || key.length == 0) {
            return;
        }

        final byte[] encoded = Multicodec.encode(codec, key);

        final String multibase = Multibase.encode(Algorithm.Base58Btc, encoded);

        builder.add(property,
                Json.createArrayBuilder().add(
                        Json.createObjectBuilder()
                            .add(Keywords.TYPE, "https://w3id.org/security#multibase")
                            .add(Keywords.VALUE, multibase)
                        )
                    );
    }
}