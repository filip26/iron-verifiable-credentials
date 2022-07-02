package com.apicatalog.ld.signature.proof;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public abstract class EmbeddedProofAdapter implements ProofAdapter {

    protected static final String BASE = "https://w3id.org/security#";

    protected static final String CREATED = "http://purl.org/dc/terms/created";

    protected static final String PROOF = "proof";
    protected static final String PROOF_PURPOSE = "proofPurpose";
    protected static final String PROOF_VERIFICATION_METHOD = "verificationMethod";
    protected static final String PROOF_DOMAIN = "domain";
    protected static final String PROOF_VALUE = "proofValue";

    protected static final String MULTIBASE_TYPE = "https://w3id.org/security#multibase";

    protected final String type;
    protected final VerificationMethodAdapter keyAdapter;

    protected EmbeddedProofAdapter(String type, VerificationMethodAdapter keyAdapter) {
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
     * @throws DocumentError
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
        return Json.createObjectBuilder(proof).remove(BASE + PROOF_VALUE).build();
    }

    protected abstract byte[] decodeValue(String encoding, String value) throws DocumentError;
    protected abstract String encodeValue(String encoding, byte[] value) throws DocumentError;

    protected void read(Proof proof, JsonObject proofObject) throws DocumentError {
        // proofPurpose property
        proof.purpose = JsonLdUtils.assertId(proofObject, BASE, PROOF_PURPOSE);

        // verificationMethod property
        if (!proofObject.containsKey(BASE + PROOF_VERIFICATION_METHOD)) {
            throw new DocumentError(ErrorType.Missing, PROOF_VERIFICATION_METHOD);
        }

        final JsonValue verificationMethodValue = proofObject.get(BASE + PROOF_VERIFICATION_METHOD);

        if (JsonUtils.isArray(verificationMethodValue) && verificationMethodValue.asJsonArray().size() > 0) {

            final JsonValue verificationMethodItem = verificationMethodValue.asJsonArray().get(0);

            if (JsonUtils.isNonEmptyObject(verificationMethodItem)) {
                proof.verificationMethod = keyAdapter.deserialize(verificationMethodItem.asJsonObject());

            } else {
                throw new DocumentError(ErrorType.Invalid, PROOF_VERIFICATION_METHOD);
            }

        } else {
            throw new DocumentError(ErrorType.Invalid, PROOF_VERIFICATION_METHOD);
        }

        // proofValue property
        if (!proofObject.containsKey(BASE + PROOF_VALUE)) {
            throw new DocumentError(ErrorType.Missing, PROOF, Keywords.VALUE);
        }

        final JsonValue embeddedProofValue = proofObject.get(BASE + PROOF_VALUE);

        if (JsonUtils.isArray(embeddedProofValue)) {

            if (!embeddedProofValue.asJsonArray().stream().allMatch(ValueObject::isValueObject)
                    || !embeddedProofValue.asJsonArray().stream()
                            .map(JsonValue::asJsonObject)
                            .map(o -> o.get(Keywords.VALUE))
                            .allMatch(JsonUtils::isString)
                    ) {
                throw new DocumentError(ErrorType.Invalid, PROOF, Keywords.VALUE);
            }


            String proofValueType = embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.TYPE);

            String encodedProofValue =  embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.VALUE);

            proof.value = decodeValue(proofValueType, encodedProofValue);

        } else {
            throw new DocumentError(ErrorType.Invalid, PROOF, Keywords.VALUE);
        }

        // created property
        if (!proofObject.containsKey(CREATED)) {
            throw new DocumentError(ErrorType.Missing, "created");
        }

        final JsonValue createdValue = proofObject.get(CREATED);

        if (JsonUtils.isArray(createdValue)) {

            // take first created property
            final JsonValue createdItem = createdValue.asJsonArray().get(0);

            // expect value object and date in ISO 8601 format
            if (!ValueObject.isValueObject(createdItem)) {
                throw new DocumentError(ErrorType.Invalid, "created");
            }

            final String createdString =
                        ValueObject
                            .getValue(createdItem)
                            .filter(JsonUtils::isString)
                            .map(JsonString.class::cast)
                            .map(JsonString::getString)
                            .orElseThrow(() -> new DocumentError(ErrorType.Invalid, "created"));

            try {
                OffsetDateTime created = OffsetDateTime.parse(createdString);

                proof.created = created.toInstant();

            } catch (DateTimeParseException e) {
                throw new DocumentError(ErrorType.Invalid, "created");
            }


        } else {
            throw new DocumentError(ErrorType.Invalid, "created");
        }

        // domain property
        if (proofObject.containsKey(BASE + PROOF_DOMAIN)) {
            proof.domain =
                ValueObject
                    .getValue(proofObject.get(BASE + PROOF_DOMAIN).asJsonArray().get(0))
                    .filter(JsonUtils::isString)
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, PROOF_DOMAIN));
        }
    }

    protected JsonObjectBuilder write(final JsonObjectBuilder builder, final Proof proof) throws DocumentError {


    builder.add(Keywords.TYPE, Json.createArrayBuilder().add(proof.getType()));

        if (proof.verificationMethod != null) {
            builder.add(BASE + PROOF_VERIFICATION_METHOD,
                    Json.createArrayBuilder()
                            .add(keyAdapter.serialize(proof.verificationMethod)));
        }

        if (proof.created != null) {
            JsonLdUtils.setValue(builder, CREATED, proof.created);
        }

        if (proof.purpose != null) {
            JsonLdUtils.setId(builder, BASE + PROOF_PURPOSE, proof.purpose);
        }

        if (proof.domain != null) {
            JsonLdUtils.setValue(builder, BASE + PROOF_DOMAIN, proof.domain);
        }

        if (proof.value != null) {
            final String proofValue = encodeValue(MULTIBASE_TYPE, proof.value);

            JsonLdUtils.setValue(builder, BASE + PROOF_VALUE, MULTIBASE_TYPE, proofValue);
        }

        return builder;
    }

    @Override
    public JsonObject setProofValue(final JsonObject proof, final byte[] value) throws DocumentError {

        final String proofValue = encodeValue(MULTIBASE_TYPE, value);

        return JsonLdUtils.setValue(Json.createObjectBuilder(proof), BASE + PROOF_VALUE, MULTIBASE_TYPE, proofValue).build();
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
