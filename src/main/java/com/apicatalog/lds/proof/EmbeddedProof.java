package com.apicatalog.lds.proof;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.NodeObject;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.lds.DataError;
import com.apicatalog.lds.DataError.ErrorType;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class EmbeddedProof implements Proof {

    public static final String CREATED = "http://purl.org/dc/terms/created";
    public static final String PROOF = "https://w3id.org/security#proof";
    public static final String PROOF_PURPOSE = "https://w3id.org/security#proofPurpose";
    public static final String PROOF_VERIFICATION_METHOD = "https://w3id.org/security#verificationMethod";
    public static final String PROOF_DOMAIN = "https://w3id.org/security#domain";
    public static final String PROOF_VALUE = "https://w3id.org/security#proofValue";

    private String type;

    private String purpose;

    private VerificationMethod verificationMethod;

    private Instant created;

    private String domain;

    private byte[] value;

    public static EmbeddedProof from(ProofOptions options) {
        final EmbeddedProof proof = new EmbeddedProof();

        proof.type = options.getType();
        proof.verificationMethod = options.getVerificationMethod();
        proof.created = options.getCreated();
        proof.domain = options.getDomain();

        return proof;
    }

    public static boolean hasProof(JsonObject credential) {
        return credential.containsKey(EmbeddedProof.PROOF);
    }

    public static Collection<JsonValue> getProof(JsonObject credential) {

        JsonValue proofs = credential.get(EmbeddedProof.PROOF);

        if (JsonUtils.isNull(proofs)) {
            return Collections.emptyList();
        }

        if (JsonUtils.isArray(proofs)) {
            if (proofs.asJsonArray().size() == 0) {
                return Collections.emptyList();
            }
        }

        return JsonUtils.toCollection(proofs);
    }
    
    public static JsonObject removeProof(final JsonObject credential) {
       return Json.createObjectBuilder(credential).remove(PROOF).build();
    }

    /**
     *
     * @param json expanded proof
     * @param result
     * @return
     * @throws VerificationError
     */
    public static EmbeddedProof from(final JsonValue json, final DocumentLoader loader) throws DataError {

        if (json == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        // data integrity checks
        if (JsonUtils.isNotObject(json)) {
            throw new DataError(ErrorType.Invalid, "proof");
        }

        JsonObject proofObject = json.asJsonObject();

        if (proofObject.containsKey(Keywords.GRAPH)) { //TODO hack

            JsonValue proofValue = proofObject.getJsonArray(Keywords.GRAPH).get(0);     //FIXME

            if (JsonUtils.isNotObject(proofValue)) {
                throw new DataError(ErrorType.Invalid, "proof");
            }
            proofObject = proofValue.asJsonObject();
        }


        final EmbeddedProof embeddedProof = new EmbeddedProof();

        // @type property
        if (!proofObject.containsKey(Keywords.TYPE)) {
            throw new DataError(ErrorType.Missing, "proof", Keywords.TYPE);
        }

        final JsonValue typeValue = proofObject.get(Keywords.TYPE);

        if (JsonUtils.isArray(typeValue)) {

            // all @type values must be string
            if (!typeValue.asJsonArray().stream().allMatch(JsonUtils::isString)) {
                throw new DataError(ErrorType.Invalid, "proof", Keywords.TYPE);
            }

            embeddedProof.type = typeValue.asJsonArray().stream()
                                    .map(JsonString.class::cast)
                                    .map(JsonString::getString)
                                    .findAny().orElse(null);

        } else if (JsonUtils.isString(typeValue)) {
            embeddedProof.type = ((JsonString)typeValue).getString();

        } else {
            throw new DataError(ErrorType.Invalid, "proof", Keywords.TYPE);
        }

        // proofPurpose property
        if (!proofObject.containsKey(PROOF_PURPOSE)) {
            throw new DataError(ErrorType.Missing, "proofPurpose");
        }

        final JsonValue proofPurposeValue = proofObject.get(PROOF_PURPOSE);

        if (JsonUtils.isArray(proofPurposeValue)) {

            if (!proofPurposeValue.asJsonArray().stream().allMatch(NodeObject::isNodeReference)) {
                throw new DataError(ErrorType.Invalid, "proofPurpose");
            }

            embeddedProof.purpose = proofPurposeValue.asJsonArray().stream()
                                    .map(JsonValue::asJsonObject)
                                    .map(o -> o.getString(Keywords.ID))
                                    .limit(1).toArray(String[]::new)[0];
        } else {
            throw new DataError(ErrorType.Invalid, "proofPurpose");
        }

        // verificationMethod property
        if (!proofObject.containsKey(PROOF_VERIFICATION_METHOD)) {
            throw new DataError(ErrorType.Missing, "verificationMethod");
        }

        final JsonValue verificationMethodValue = proofObject.get(PROOF_VERIFICATION_METHOD);

        if (JsonUtils.isArray(verificationMethodValue) && verificationMethodValue.asJsonArray().size() > 0) {

            final JsonValue verificationMethodItem = verificationMethodValue.asJsonArray().get(0);

            if (JsonUtils.isNonEmptyObject(verificationMethodItem)) {
                embeddedProof.verificationMethod = Ed25519KeyPair2020.from(verificationMethodItem.asJsonObject());

//                } else if (NodeObject.isNodeReference(verificationMethodItem)) {
//
//                    final JsonObject verificationMethodObject = verificationMethodItem.asJsonObject();
//
//                    final String id = verificationMethodObject.getString(Keywords.ID);
//
//                    embeddedProof.verificationMethod = Ed25519KeyPair2020.reference(URI.create(id));

            } else {
                throw new DataError(ErrorType.Invalid, "verificationMethod");
            }


        } else {
            throw new DataError(ErrorType.Invalid, "verificationMethod");
        }

        // proofValue property
        if (!proofObject.containsKey(PROOF_VALUE)) {
            throw new DataError(ErrorType.Missing, "proof", Keywords.VALUE);
        }

        final JsonValue embeddedProofValue = proofObject.get(PROOF_VALUE);

        if (JsonUtils.isArray(embeddedProofValue)) {

            if (!embeddedProofValue.asJsonArray().stream().allMatch(ValueObject::isValueObject)
                    || !embeddedProofValue.asJsonArray().stream()
                            .map(JsonValue::asJsonObject)
                            .map(o -> o.get(Keywords.VALUE))
                            .allMatch(JsonUtils::isString)
                    ) {
                throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE);
            }


            String proofValueType = embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.TYPE);

            String encodedProofValue =  embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.VALUE);

            // verify supported proof value encoding
            if (!"https://w3id.org/security#multibase".equals(proofValueType)) {
                throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE);
                //FIXME belongs to ED25...
            }

            // verify proof value
            if (encodedProofValue == null || !Multibase.isAlgorithmSupported(encodedProofValue)) {
                throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE);
            }

            // decode proof value
            byte[] rawProofValue = Multibase.decode(encodedProofValue);

            // verify proof value length
            if (rawProofValue.length != 64) {
                throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE, "length");
            }

            embeddedProof.value = rawProofValue;

        } else {
            throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE);
        }

        // created property
        if (!proofObject.containsKey(CREATED)) {
            throw new DataError(ErrorType.Missing, "created");
        }

        final JsonValue createdValue = proofObject.get(CREATED);

        if (JsonUtils.isArray(createdValue)) {

            // take first created property
            final JsonValue createdItem = createdValue.asJsonArray().get(0);

            // expect value object and date in ISO 8601 format
            if (!ValueObject.isValueObject(createdItem)) {
                throw new DataError(ErrorType.Invalid, "created");
            }

            //TODO check @type

            String createdString = ValueObject.getValue(createdItem).filter(JsonUtils::isString)
            .map(JsonString.class::cast)
            .map(JsonString::getString).orElseThrow(DataError::new);

            try {
                OffsetDateTime created = OffsetDateTime.parse(createdString);

                embeddedProof.created = created.toInstant();

            } catch (DateTimeParseException e) {
                throw new DataError(ErrorType.Invalid, "created");
            }


        } else {
            throw new DataError(ErrorType.Invalid, "created");
        }


        //TODO domain property



        return embeddedProof;       //FIXME process other proofs
    }

    @Override
    public String getType() {
        return type;
    }


    @Override
    public String getPurpose() {
        return purpose;
    }

    @Override
    public VerificationMethod getVerificationMethod() {
        return verificationMethod;
    }

    @Override
    public Instant getCreated() {
        return created;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public byte[] getValue() {
        return value;
    }
    
    public void setValue(byte[] value) {
        this.value = value;
    }

    public JsonObject toJson() {

        final JsonObjectBuilder root = Json.createObjectBuilder().add(Keywords.TYPE, Json.createArrayBuilder().add(type));

        if (verificationMethod != null) {
            root.add(PROOF_VERIFICATION_METHOD,
                    Json.createArrayBuilder()
                            .add(verificationMethod.toJson()));
        }

        if (created != null) {
            root.add(CREATED,
                    Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add(Keywords.TYPE, "http://www.w3.org/2001/XMLSchema#dateTime")
                                    .add(Keywords.VALUE, created.toString())));
        }

        root.add(PROOF_PURPOSE,
                Json.createArrayBuilder()
                        .add(Json.createObjectBuilder().add(Keywords.ID,
                                "https://w3id.org/security#assertionMethod")));  //FIXME configurable

        // TODO domain to proof
        
        if (value != null) {
            
            final String proofValue = Multibase.encode(Algorithm.Base58Btc, value);
            
            root.add(PROOF_VALUE,
                    Json.createArrayBuilder().add(Json.createObjectBuilder()
                            .add(Keywords.VALUE, proofValue)
                            .add(Keywords.TYPE, "https://w3id.org/security#multibase")
                            )
                    );
        }
        return root.build();
    }

    public JsonObject setProof(final JsonObject document) {

        final JsonValue proofPropertyValue = document.get(PROOF);

        final JsonArrayBuilder proofs;

        if (proofPropertyValue == null) {
            proofs  = Json.createArrayBuilder();

        } else {
            proofs = Json.createArrayBuilder(JsonUtils.toJsonArray(proofPropertyValue));
        }

        proofs.add(toJson());

        return Json.createObjectBuilder(document).add(PROOF, proofs).build();
    }
}
