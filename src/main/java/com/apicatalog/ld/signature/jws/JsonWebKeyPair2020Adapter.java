package com.apicatalog.ld.signature.jws;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.apicatalog.ld.signature.ed25519.Ed25519KeyPair2020Adapter;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * Json Web Key 2020 Suite.
 *
 * Based on {@link Ed25519KeyPair2020Adapter}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JsonWebKeyPair2020Adapter extends JsonWebKey2020Adapter {

    protected static final String TYPE = "JsonWebKey2020";
    protected static final String PRIVATE_KEY_JWK = "privateKeyJwk";

    @Override
    public String getType() {
        return JsonWebSignature2020.BASE + TYPE;
    }

    @Override
    public VerificationMethod deserialize(JsonObject object) throws DocumentError {
//        JwsVerificationKey pubKey = new JwsVerificationKey();
//        JsonLdUtils.getId(object).ifPresent(pubKey::setId);
//        JsonLdUtils.getType(object).stream().findFirst().ifPresent(pubKey::setType);
//        //set controller and public key
//        pubKey = JsonWebKey2020Adapter.from(pubKey, object);

        JwsVerificationKey pubKey = (JwsVerificationKey) super.deserialize(object);

        JWK privateKey = null;

        if (JsonLdUtils.hasPredicate(object, JsonWebSignature2020.BASE + PRIVATE_KEY_JWK)) {
            privateKey = getKey(object, JsonWebSignature2020.BASE + PRIVATE_KEY_JWK);
        }

        JwsKeyPair keyPair = new JwsKeyPair();
        keyPair.setId(pubKey.id());
        keyPair.setType(pubKey.type());
        keyPair.setController(pubKey.controller());
        keyPair.setPublicKey(pubKey.getPublicKey());
        keyPair.setPrivateKey(privateKey);

        return keyPair;
    }

    @Override
    public JsonObject serialize(VerificationMethod proof) {

        final JsonObjectBuilder builder = Json.createObjectBuilder(super.serialize(proof));

        if (proof instanceof JwsKeyPair) {
            setKey(builder, ((JwsKeyPair) proof).getPrivateKey(), PRIVATE_KEY_JWK);
        }

        return builder.build();

    }

}
