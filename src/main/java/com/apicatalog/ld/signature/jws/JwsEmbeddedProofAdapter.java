package com.apicatalog.ld.signature.jws;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
//import com.apicatalog.ld.signature.proof.EmbeddedProofAdapter;
import com.apicatalog.ld.signature.jws.from_lib_v070.VerificationMethodAdapter;
import jakarta.json.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 *
 * Based on com.apicatalog.ld.signature.proof.EmbeddedProofAdapter
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public abstract class JwsEmbeddedProofAdapter implements JwsProofAdapter {

    protected static final String BASE = "https://w3id.org/security#";

    protected static final String CREATED = "http://purl.org/dc/terms/created";

    protected static final String PROOF = "proof";
    protected static final String PROOF_PURPOSE = "proofPurpose";
    protected static final String PROOF_VERIFICATION_METHOD = "verificationMethod";
    protected static final String PROOF_DOMAIN = "domain";
//    protected static final String PROOF_VALUE = "proofValue";
    protected static final String JWS = "jws";

//    protected static final String MULTIBASE_TYPE = "https://w3id.org/security#multibase";

    protected final String type;
    protected final VerificationMethodAdapter keyAdapter;

    protected JwsEmbeddedProofAdapter(String type, VerificationMethodAdapter keyAdapter) {
        this.type = type;
        this.keyAdapter = keyAdapter;
    }

    /**
     * Appends the proof to the given VC/VP document.
     * If the document has been signed already then the proof is added into a proof set.
     *
     * @param document VC/VP document
     * @param proof
     *
     * @return the given VC/VP with the proof attached
     *
     *
     */
    public static JsonObject addProof(final JsonObject document, final JsonObject proof) {

        final JsonValue proofPropertyValue = document.get(BASE + PROOF);

        return Json
                .createObjectBuilder(document)
                .add(BASE + PROOF,
                        ((proofPropertyValue != null)
                                ? Json.createArrayBuilder(JsonUtils.toJsonArray(proofPropertyValue))
                                : Json.createArrayBuilder()
                        )
                                .add(proof)
                )
                .build();
    }

    public static boolean hasProof(JsonObject proof) {
        return proof.containsKey(BASE + PROOF);
    }

    public static Collection<JsonValue> getProof(JsonObject proof) {
        return JsonLdUtils.getObjects(proof, BASE + PROOF);
    }

    public static JsonObject removeProof(final JsonObject proof) {
        return Json.createObjectBuilder(proof).remove(BASE + PROOF).build();
    }

    public static JsonObject removeProofValue(final JsonObject proof) {
        return Json.createObjectBuilder(proof).remove(BASE + JWS).build();
    }

//    protected abstract byte[] decodeValue(String encoding, String value) throws DocumentError;
//    protected abstract String encodeValue(String encoding, byte[] value) throws DocumentError;

    /**
     * Taken from read() function of com.apicatalog.ld.signature.proof.EmbeddedProofAdapter
     * and updated to our needs
     */
    protected void read(JwsProof proof, JsonObject proofObject) throws DocumentError {
        // proofPurpose property
        proof.purpose = JsonLdUtils.assertId(proofObject, BASE, PROOF_PURPOSE);

        // verificationMethod property
        if (!proofObject.containsKey(BASE + PROOF_VERIFICATION_METHOD)) {
            throw new DocumentError(DocumentError.ErrorType.Missing, PROOF_VERIFICATION_METHOD);
        }

        final JsonValue verificationMethodValue = proofObject.get(BASE + PROOF_VERIFICATION_METHOD);

        if (JsonUtils.isArray(verificationMethodValue) && verificationMethodValue.asJsonArray().size() > 0) {

            final JsonValue verificationMethodItem = verificationMethodValue.asJsonArray().get(0);

            if (JsonUtils.isNonEmptyObject(verificationMethodItem)) {
                proof.verificationMethod = keyAdapter.deserialize(verificationMethodItem.asJsonObject());

            } else {
                throw new DocumentError(DocumentError.ErrorType.Invalid, PROOF_VERIFICATION_METHOD);
            }

        } else {
            throw new DocumentError(DocumentError.ErrorType.Invalid, PROOF_VERIFICATION_METHOD);
        }

        // jws property
        if (!proofObject.containsKey(BASE + JWS)) {
            throw new DocumentError(DocumentError.ErrorType.Missing, PROOF, Keywords.VALUE);
        }

        final JsonValue embeddedProofValue = proofObject.get(BASE + JWS);

        if (JsonUtils.isArray(embeddedProofValue)) {
//            if (!embeddedProofValue.asJsonArray().stream().allMatch(ValueObject::isValueObject)
//                    || !embeddedProofValue.asJsonArray().stream()
//                    .map(JsonValue::asJsonObject)
//                    .map(o -> o.get(Keywords.VALUE))
//                    .allMatch(JsonUtils::isString)
//            ) {
//                throw new DocumentError(DocumentError.ErrorType.Invalid, PROOF, Keywords.VALUE);
//            }
//
//
//            String proofValueType = embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.TYPE);
//
//            String encodedProofValue =  embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.VALUE);
//
//            proof.value = decodeValue(proofValueType, encodedProofValue);

            final JsonValue jwsItem = embeddedProofValue.asJsonArray().get(0);

            if (JsonUtils.isNonEmptyObject(jwsItem)) {
                proof.jws = jwsItem.asJsonObject().getString(Keywords.VALUE);

            } else {
                throw new DocumentError(DocumentError.ErrorType.Invalid, JWS);
            }

        } else {
            throw new DocumentError(DocumentError.ErrorType.Invalid, PROOF, Keywords.VALUE);
        }

        // created property
        if (!proofObject.containsKey(CREATED)) {
            throw new DocumentError(DocumentError.ErrorType.Missing, "created");
        }

        final JsonValue createdValue = proofObject.get(CREATED);

        if (JsonUtils.isArray(createdValue)) {

            // take first created property
            final JsonValue createdItem = createdValue.asJsonArray().get(0);

            // expect value object and date in ISO 8601 format
            if (!ValueObject.isValueObject(createdItem)) {
                throw new DocumentError(DocumentError.ErrorType.Invalid, "created");
            }

            final String createdString =
                    ValueObject
                            .getValue(createdItem)
                            .filter(JsonUtils::isString)
                            .map(JsonString.class::cast)
                            .map(JsonString::getString)
                            .orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Invalid, "created"));

            try {
                OffsetDateTime created = OffsetDateTime.parse(createdString);

                proof.created = created.toInstant();

            } catch (DateTimeParseException e) {
                throw new DocumentError(DocumentError.ErrorType.Invalid, "created");
            }


        } else {
            throw new DocumentError(DocumentError.ErrorType.Invalid, "created");
        }

        // domain property
        if (proofObject.containsKey(BASE + PROOF_DOMAIN)) {
            proof.domain =
                    ValueObject
                            .getValue(proofObject.get(BASE + PROOF_DOMAIN).asJsonArray().get(0))
                            .filter(JsonUtils::isString)
                            .map(JsonString.class::cast)
                            .map(JsonString::getString)
                            .orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Invalid, PROOF_DOMAIN));
        }
    }

    /**
     * Taken from write() function of com.apicatalog.ld.signature.proof.EmbeddedProofAdapter
     * and updated to our needs
     */
    protected JsonObjectBuilder write(final JsonObjectBuilder builder, final JwsProof proof) throws DocumentError {


        builder.add(Keywords.TYPE, Json.createArrayBuilder().add(proof.getType()));

        if (proof.getVerificationMethod() != null) {
            builder.add(BASE + PROOF_VERIFICATION_METHOD,
                    Json.createArrayBuilder()
                            .add(keyAdapter.serialize(proof.getVerificationMethod())));
        }

        if (proof.getCreated() != null) {
            JsonLdUtils.setValue(builder, CREATED, proof.getCreated());
        }

        if (proof.getPurpose() != null) {
            JsonLdUtils.setId(builder, BASE + PROOF_PURPOSE, proof.getPurpose());
        }

        if (proof.getDomain() != null) {
            JsonLdUtils.setValue(builder, BASE + PROOF_DOMAIN, proof.getDomain());
        }

        if (proof.getJws() != null) {
//            final String proofValueB64 = Base64URL.encode(proof.getValue()).toString();
//            final String jws = header + ".." + proofValueB64; //eg: {"alg":"ES256K","b64": false,"crit": ["b64"]}
//            JsonLdUtils.setValue(builder, BASE + "jws", jws);
            JsonLdUtils.setValue(builder, BASE + "jws", proof.getJws());
        }

        return builder;
    }

    /**
     * Taken from com.apicatalog.ld.signature.proof.EmbeddedProofAdapter#setProofValue(JsonObject, byte[]) and updated to our needs
     *
     * @param proof proof
     * @param jws JWS (Json Web Signature) with unencoded (detached) payload
     * @return
     * @throws DocumentError
     */
    @Override
    public JsonObject setProofValue(final JsonObject proof, String jws) throws DocumentError {
        return JsonLdUtils.setValue(Json.createObjectBuilder(proof), BASE + "jws", jws).build();
//        return JsonLdUtils.setValue(Json.createArrayBuilder().add(Json.createObjectBuilder(proof)), BASE + "jws", jws).build();
    }

    @Override
    public VerificationMethodAdapter getMethodAdapter() {
        return keyAdapter;
    }

    @Override
    public String type() {
        return type;
    }

}
