package com.apicatalog.ld.signature.ed25519;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.apicatalog.ld.signature.proof.VerificationMethodAdapter;
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
public class Ed25519VerificationKey2020Adapter implements VerificationMethodAdapter {

    private static final String TYPE = "Ed25519VerificationKey2020";

    protected static final String CONTROLLER = "controller";
    protected static final String PUBLIC_KEY_MULTIBASE = "publicKeyMultibase";
    protected static final String PUBLIC_KEY_TYPE_VALUE = "https://w3id.org/security#multibase";

    @Override
    public String getType() {
	return Ed25519Signature2020.BASE + TYPE;
    }

    @Override
    public VerificationMethod deserialize(JsonObject object) throws DataError {
	
        final VerificationKey key = new VerificationKey();
        
        JsonLdUtils.getId(object).ifPresent(key::setId);
        JsonLdUtils.getType(object).stream().findFirst().ifPresent(key::setType);

        return from(key, object);
    }

    @Override
    public JsonObject serialize(VerificationMethod proof) {
        return serialize(Json.createObjectBuilder(), proof).build();
    }

    static final VerificationKey from(VerificationKey key, JsonObject json) throws DataError {

        // controller
        JsonLdUtils.getObjects(json, Ed25519Signature2020.BASE + CONTROLLER)
            .stream()
            .findFirst()
            .ifPresent(controller -> {

                if (JsonUtils.isArray(controller)) {
                    controller = controller.asJsonArray().get(0);
                }

                JsonLdUtils.getId(controller)
                    .ifPresent(id -> {
                        key.setController(id);
                    });
            });

        // public key
        if (JsonLdUtils.hasPredicate(json, Ed25519Signature2020.BASE + PUBLIC_KEY_MULTIBASE)) {
            key.setPublicKey(getKey(json, Ed25519Signature2020.BASE + PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey));

            // verify verification key length
            if (key.getPublicKey() != null
                    && key.getPublicKey().length != 32
                    && key.getPublicKey().length != 57
                    && key.getPublicKey().length != 114
                    ) {
                throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE, "length");
            }
        }
        return key;
    }


    static byte[] getKey(JsonObject json, String property, Codec expected) throws DataError {

        JsonValue key = JsonLdUtils
                            .getObjects(json, property)
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new DataError(ErrorType.Missing, Keywords.TYPE));

        if (JsonUtils.isArray(key)) {
            key = key.asJsonArray().get(0);
        }

        if (!ValueObject.isValueObject(key)) {
            throw new DataError(ErrorType.Invalid, property);
        }

        if (!JsonLdUtils.isTypeOf(PUBLIC_KEY_TYPE_VALUE, key.asJsonObject())) {
            throw new DataError(ErrorType.Invalid, property, Keywords.TYPE);
        }

        final String keyMultibase = ValueObject
        				.getValue(key)
        				.filter(JsonUtils::isString)
        				.map(JsonString.class::cast)
        				.map(JsonString::getString)
        				.orElseThrow(() -> new DataError(ErrorType.Invalid, property));
        // decode private key
        final byte[] encodedKey = Multibase.decode(keyMultibase);

        final Codec codec = Multicodec
        			.codec(Type.Key, encodedKey)
        			.orElseThrow(() -> new DataError(ErrorType.Invalid, property));

        if (expected != codec) {
            throw new DataError(ErrorType.Invalid, property);
        }

        return Multicodec.decode(codec, encodedKey);
    }

    static byte[] decodeKey(final String multibase) throws DataError {

        // decode private key
        final byte[] encodedKey = Multibase.decode(multibase);

        final Codec codec = Multicodec
        			.codec(Type.Key, encodedKey)
        			.orElseThrow(() -> new DataError(ErrorType.Invalid, "key"));

        return Multicodec.decode(codec, encodedKey);
    }

    static JsonObjectBuilder serialize(JsonObjectBuilder builder, VerificationMethod key) {
        if (key.getId() != null) {
            builder.add(Keywords.ID, key.getId().toString());
        }

        if (key.getType() != null) {
            builder.add(Keywords.TYPE, key.getType());            
        }

        if (key.getController()!= null) {
            JsonLdUtils.setId(builder, Ed25519Signature2020.BASE + CONTROLLER, key.getController());
        }
        
        if (key instanceof VerificationKey) {
            return setKey(builder, ((VerificationKey)key).getPublicKey(), Ed25519Signature2020.BASE + PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);
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