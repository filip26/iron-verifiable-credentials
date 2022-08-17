package com.apicatalog.ld.signature.jws;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.ed25519.Ed25519VerificationKey2020Adapter;
import com.apicatalog.ld.signature.jws.from_lib_v070.VerificationMethodAdapter;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.*;

import java.text.ParseException;
//import com.apicatalog.ld.signature.proof.VerificationMethodAdapter;

/**
 * Json Web Key 2020 Suite.
 *
 * Based on {@link Ed25519VerificationKey2020Adapter}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JsonWebKey2020Adapter implements VerificationMethodAdapter {


    protected static final String TYPE = "JsonWebKey2020";
    protected static final String CONTROLLER = "controller";
    protected static final String PUBLIC_KEY_JWK = "publicKeyJwk";
    protected static final String PUBLIC_KEY_TYPE_VALUE = "https://w3id.org/security#publicKeyJwk";

    @Override
    public String getType() {
        return JsonWebSignature2020.BASE + TYPE;
    }

    @Override
    public VerificationMethod deserialize(JsonObject object) throws DocumentError {

        final JwsVerificationKey key = new JwsVerificationKey();

        JsonLdUtils.getId(object).ifPresent(key::setId);
        JsonLdUtils.getType(object).stream().findFirst().ifPresent(key::setType);

        return from(key, object);
    }

    @Override
    public JsonObject serialize(VerificationMethod proof) {
        return serialize(Json.createObjectBuilder(), proof).build();
    }

    static JwsVerificationKey from(JwsVerificationKey key, JsonObject json) throws DocumentError {

        // controller
        JsonLdUtils.getObjects(json, JsonWebSignature2020.BASE + CONTROLLER)
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
        if (JsonLdUtils.hasPredicate(json, JsonWebSignature2020.BASE + PUBLIC_KEY_JWK)) {
            key.setPublicKey(getKey(json, JsonWebSignature2020.BASE + PUBLIC_KEY_JWK));
        }
        return key;
    }

    static JWK getKey(JsonObject json, String property) throws DocumentError {

        JsonValue key = JsonLdUtils
                .getObjects(json, property)
                .stream()
                .findFirst()
                .orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Missing, Keywords.TYPE));

        if (JsonUtils.isArray(key)) {
            key = key.asJsonArray().get(0);
        }

        if (!ValueObject.isValueObject(key)) {
            throw new DocumentError(DocumentError.ErrorType.Invalid, property);
        }

        if (!JsonLdUtils.isTypeOf(PUBLIC_KEY_TYPE_VALUE, key.asJsonObject())) {
            throw new DocumentError(DocumentError.ErrorType.Invalid, property, Keywords.TYPE);
        }

        final String jwkString = ValueObject
                .getValue(key)
                .filter(JsonUtils::isString)
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Invalid, property));

        try {
            return JWK.parse(jwkString);
        } catch (ParseException e) {
            throw new DocumentError(DocumentError.ErrorType.Invalid, e.getMessage());
        }

    }

    static JsonObjectBuilder serialize(JsonObjectBuilder builder, VerificationMethod key) {
        if (key.id() != null) {
            builder.add(Keywords.ID, key.id().toString());
        }

        if (key.type() != null) {
            builder.add(Keywords.TYPE, key.type());
        }

        if (key.controller()!= null) {
            JsonLdUtils.setId(builder, JsonWebSignature2020.BASE + CONTROLLER, key.controller());
        }

        if (key instanceof JwsVerificationKey) {
            return setKey(builder, ((JwsVerificationKey)key).getPublicKey(), JsonWebSignature2020.BASE + PUBLIC_KEY_JWK);
        }

        return builder;
    }

    static JsonObjectBuilder setKey(JsonObjectBuilder builder, JWK key, String property) {

        if (key == null) {
            return builder;
        }

        final String jwkString = key.toString();

        return JsonLdUtils.setValue(builder, property, PUBLIC_KEY_TYPE_VALUE, jwkString);
    }

}
