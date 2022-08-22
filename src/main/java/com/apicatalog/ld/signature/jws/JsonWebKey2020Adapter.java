package com.apicatalog.ld.signature.jws;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.ed25519.Ed25519VerificationKey2020Adapter;
import com.apicatalog.ld.signature.json.VerificationMethodJsonAdapter;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.*;

import java.text.ParseException;

import static com.apicatalog.jsonld.JsonLdUtils.jakartaJsonObjToString;
import static com.apicatalog.jsonld.JsonLdUtils.stringToJakartaJsonObj;

/**
 * Json Web Key 2020 Suite.
 *
 * Based on {@link Ed25519VerificationKey2020Adapter}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JsonWebKey2020Adapter implements VerificationMethodJsonAdapter {


    protected static final String TYPE = "JsonWebKey2020";
    protected static final String CONTROLLER = "controller";
    protected static final String PUBLIC_KEY_JWK = "publicKeyJwk";
    protected static final String KEY_TYPE_VALUE = "@json";

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

        if (!JsonLdUtils.isTypeOf(KEY_TYPE_VALUE, key.asJsonObject())) {
            throw new DocumentError(DocumentError.ErrorType.Invalid, property, Keywords.TYPE);
        }

//        final String jwkString = ValueObject
//                .getValue(key)
//                .filter(JsonUtils::isString)
//                .map(JsonString.class::cast)
//                .map(JsonString::getString)
//                .orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Invalid, property));

//        JsonValue jsonValue = ValueObject.getValue(key).orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Invalid, property));
//        if(JsonUtils.isString(jsonValue))
//            System.out.println("getKey - jsonValue is string = " + jsonValue.toString());
//        else if (JsonUtils.isObject(jsonValue))
//            System.out.println("getKey - jsonValue is object = " + jakartaJsonObjToString((JsonObject) jsonValue));

        String jwkString = ValueObject
                .getValue(key)
                .filter(JsonUtils::isObject)
                .map(JsonObject.class::cast) //map JsonValue to JsonObject
                .map(keyJsonObj -> {         //map JsonObject to String
                    return jakartaJsonObjToString(keyJsonObj);
                })
                .orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Invalid, property));

        System.out.println("getKey - jwkString = " + jwkString);

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

        System.out.println("setKey - jwkString = " + jwkString);

        //@value
        final JsonObject value = stringToJakartaJsonObj(jwkString);
//        return JsonLdUtils.setValue(builder, property, KEY_TYPE_VALUE, value); //does not take JsonObject, only String

        //entry of https://w3id.org/security#publicKeyJwk or https://w3id.org/security#privateKeyJwk json parameter
        JsonObject keyJwkObject = Json.createObjectBuilder()
                .add(Keywords.TYPE, KEY_TYPE_VALUE)             //set value of json parameter @type
                .add(Keywords.VALUE, value)                     //set value of json parameter @value
                .build();
        //value of https://w3id.org/security#publicKeyJwk or https://w3id.org/security#privateKeyJwk json parameter
        JsonArray keyJwkObjectArray = Json.createArrayBuilder().add(keyJwkObject).build();

//        //example of "keyJwkObjectArray" json array with "keyJwkObject" json object inside:
//        "https://w3id.org/security#publicKeyJwk":[
//            {
//                "@type":"@json",
//                "@value": {
//                    "kty": "EC",
//                    "crv": "P-384",
//                    "x": "eQbMauiHc9HuiqXT894gW5XTCrOpeY8cjLXAckfRtdVBLzVHKaiXAAxBFeVrSB75",
//                    "y": "YOjxhMkdH9QnNmGCGuGXJrjAtk8CQ1kTmEEi9cg2R9ge-zh8SFT1Xu6awoUjK5Bv"
//                }
//            }
//        ]

        return builder.add(property, keyJwkObjectArray);
    }

}
