package com.apicatalog.ld.signature.ed25519;

import java.net.URI;
import java.util.Optional;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.json.VerificationMethodJsonAdapter;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.VerificationMethod;
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

/**
 * Ed25519 Verification Key 2020 Suite.
 */
public class Ed25519VerificationKey2020Adapter implements VerificationMethodJsonAdapter {

    protected static final String TYPE = "Ed25519VerificationKey2020";
    protected static final String CONTROLLER = "controller";
    protected static final String PUBLIC_KEY_MULTIBASE = "publicKeyMultibase";
    protected static final String PUBLIC_KEY_TYPE_VALUE = "https://w3id.org/security#multibase";

    @Override
    public String getType() {
        return Ed25519Signature2020.BASE + TYPE;
    }

    @Override
    public VerificationMethod deserialize(JsonObject object) throws DocumentError {
 
        URI id = JsonLdUtils.getId(object).orElse(null);
        
        URI controller = controllerFrom(object).orElse(null);
        
        String type = JsonLdUtils.getType(object).stream().findFirst().orElse(null);
        
        byte[] publicKey = publicKeyFrom(object).orElse(null);

        return new Ed25519VerificationKey2020(
                        id,
                        controller,
                        type,
                        publicKey
                        );
    }

    @Override
    public JsonObject serialize(VerificationMethod proof) {
        return serialize(Json.createObjectBuilder(), proof).build();
    }
    
    static final Optional<URI> controllerFrom(JsonObject json) throws DocumentError {

        // controller
        return JsonLdUtils.getObjects(json, Ed25519Signature2020.BASE + CONTROLLER)
            .stream()
            .findFirst()
            .map(controller -> {

                if (JsonUtils.isArray(controller)) {
                    controller = controller.asJsonArray().get(0);
                }

                return JsonLdUtils.getId(controller).orElse(null);
            });
    }
    
    static final Optional<byte[]> publicKeyFrom(JsonObject json) throws DocumentError {

        // public key
        if (JsonLdUtils.hasPredicate(json, Ed25519Signature2020.BASE + PUBLIC_KEY_MULTIBASE)) {
            
            byte[] publicKey = getKey(json, Ed25519Signature2020.BASE + PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey); 
            
            // verify verification key length
            if (publicKey != null
                    && publicKey.length != 32
                    && publicKey.length != 57
                    && publicKey.length != 114
                    ) {
                throw new DocumentError(ErrorType.Invalid, "proof", Keywords.VALUE, "length");
            }

            return Optional.ofNullable(publicKey);
        }

        return Optional.empty();
    }

    static byte[] getKey(JsonObject json, String property, Codec expected) throws DocumentError {

        JsonValue key = JsonLdUtils
                            .getObjects(json, property)
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new DocumentError(ErrorType.Missing, Keywords.TYPE));

        if (JsonUtils.isArray(key)) {
            key = key.asJsonArray().get(0);
        }

        if (!ValueObject.isValueObject(key)) {
            throw new DocumentError(ErrorType.Invalid, property);
        }

        if (!JsonLdUtils.isTypeOf(PUBLIC_KEY_TYPE_VALUE, key.asJsonObject())) {
            throw new DocumentError(ErrorType.Invalid, property, Keywords.TYPE);
        }

        final String keyMultibase = ValueObject
                        .getValue(key)
                        .filter(JsonUtils::isString)
                        .map(JsonString.class::cast)
                        .map(JsonString::getString)
                        .orElseThrow(() -> new DocumentError(ErrorType.Invalid, property));
        // decode private key
        final byte[] encodedKey = Multibase.decode(keyMultibase);

        final Codec codec = Multicodec
                    .codec(Type.Key, encodedKey)
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, property));

        if (expected != codec) {
            throw new DocumentError(ErrorType.Invalid, property);
        }

        return Multicodec.decode(codec, encodedKey);
    }

    static byte[] decodeKey(final String multibase) throws DocumentError {

        // decode private key
        final byte[] encodedKey = Multibase.decode(multibase);

        final Codec codec = Multicodec
                    .codec(Type.Key, encodedKey)
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, "key"));

        return Multicodec.decode(codec, encodedKey);
    }

    static JsonObjectBuilder serialize(JsonObjectBuilder builder, VerificationMethod key) {
        if (key.id() != null) {
            builder.add(Keywords.ID, key.id().toString());
        }

        if (key.type() != null) {
            builder.add(Keywords.TYPE, key.type());
        }

        if (key.controller()!= null) {
            JsonLdUtils.setId(builder, Ed25519Signature2020.BASE + CONTROLLER, key.controller());
        }

        if (key instanceof VerificationKey) {
            return setKey(builder, ((VerificationKey)key).publicKey(), Ed25519Signature2020.BASE + PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);
        }

        return builder;
    }

    static JsonObjectBuilder setKey(JsonObjectBuilder builder, byte[] key, String property, Codec codec) {

        if (key == null || key.length == 0) {
            return builder;
        }

        final byte[] encoded = Multicodec.encode(codec, key);

        final String multibase = Multibase.encode(Algorithm.Base58Btc, encoded);

        return JsonLdUtils.setValue(builder, property, PUBLIC_KEY_TYPE_VALUE, multibase);
    }
}