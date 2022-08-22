package com.apicatalog.ld.signature.jws;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.ed25519.Ed25519Proof2020Adapter;
import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 *
 * Based on {@link Ed25519Proof2020Adapter}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JsonWebProof2020Adapter extends JwsEmbeddedProofAdapter {

    public static final String TYPE = "https://w3id.org/security#JsonWebSignature2020";

    public JsonWebProof2020Adapter() {
        super(TYPE, new JsonWebKey2020Adapter());
    }

    @Override
    public JwsProof deserialize(JsonObject object) throws DocumentError {
        if (object == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        // data integrity checks
        if (JsonUtils.isNotObject(object)) {
            throw new DocumentError(ErrorType.Invalid, PROOF);
        }

        final JsonObject proofObject = object.asJsonObject();

        if (!JsonLdUtils.isTypeOf(TYPE, proofObject)) {

            // @type property
            if (!JsonLdUtils.hasType(proofObject)) {
                throw new DocumentError(ErrorType.Missing, PROOF, Keywords.TYPE);
            }

            throw new DocumentError(ErrorType.Unknown, "cryptoSuite", Keywords.TYPE);
        }

        final JwsProof proof = new JwsProof();

        read(proof, proofObject);

        return proof;
    }

    @Override
    public JsonObject serialize(JwsProof proof) throws DocumentError {
        return write(Json.createObjectBuilder(), proof).build();
    }

}
