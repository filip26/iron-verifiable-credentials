package com.apicatalog.ld.signature.proof;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.ed25519.Ed25519KeyPair2020;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public abstract class EmbeddedProof implements Proof {

    protected static final String BASE = "https://w3id.org/security#";
    
    protected static final String CREATED = "http://purl.org/dc/terms/created";
    
    protected static final String PROOF = "proof";
    protected static final String PROOF_PURPOSE = "proofPurpose";
    protected static final String PROOF_VERIFICATION_METHOD = "verificationMethod";
    protected static final String PROOF_DOMAIN = "https://w3id.org/security#domain";
    protected static final String PROOF_VALUE = "proofValue";
    
    protected static final String MULTIBASE_TYPE = "https://w3id.org/security#multibase";

    protected URI purpose;

    protected VerificationMethod verificationMethod;

    protected Instant created;

    protected String domain;

    protected byte[] value;
    
    protected EmbeddedProof() {}

    public static boolean hasProof(JsonObject credential) {
        return credential.containsKey(BASE + PROOF);
    }

    public static Collection<JsonValue> getProof(JsonObject credential) {
        return JsonLdUtils.getObjects(credential, BASE + PROOF);
    }
    
    public static JsonObject removeProof(final JsonObject credential) {
       return Json.createObjectBuilder(credential).remove(BASE + PROOF).build();
    }

    public static JsonObject removeProofValue(final JsonObject credential) {
        return Json.createObjectBuilder(credential).remove(BASE + PROOF_VALUE).build();
     }

    protected static EmbeddedProof from(EmbeddedProof embeddedProof, final JsonObject proofObject, final DocumentLoader loader) throws DataError {

        // proofPurpose property
        embeddedProof.purpose = JsonLdUtils.assertId(proofObject, BASE, PROOF_PURPOSE);
        
        // verificationMethod property
        if (!proofObject.containsKey(BASE + PROOF_VERIFICATION_METHOD)) {
            throw new DataError(ErrorType.Missing, PROOF_VERIFICATION_METHOD);
        }

        final JsonValue verificationMethodValue = proofObject.get(BASE + PROOF_VERIFICATION_METHOD);

        if (JsonUtils.isArray(verificationMethodValue) && verificationMethodValue.asJsonArray().size() > 0) {

            final JsonValue verificationMethodItem = verificationMethodValue.asJsonArray().get(0);

            if (JsonUtils.isNonEmptyObject(verificationMethodItem)) {
                embeddedProof.verificationMethod = Ed25519KeyPair2020.from(verificationMethodItem.asJsonObject());

            } else {
                throw new DataError(ErrorType.Invalid, PROOF_VERIFICATION_METHOD);
            }

        } else {
            throw new DataError(ErrorType.Invalid, PROOF_VERIFICATION_METHOD);
        }

        // proofValue property
        if (!proofObject.containsKey(BASE + PROOF_VALUE)) {
            throw new DataError(ErrorType.Missing, PROOF, Keywords.VALUE);
        }

        final JsonValue embeddedProofValue = proofObject.get(BASE + PROOF_VALUE);

        if (JsonUtils.isArray(embeddedProofValue)) {

            if (!embeddedProofValue.asJsonArray().stream().allMatch(ValueObject::isValueObject)
                    || !embeddedProofValue.asJsonArray().stream()
                            .map(JsonValue::asJsonObject)
                            .map(o -> o.get(Keywords.VALUE))
                            .allMatch(JsonUtils::isString)
                    ) {
                throw new DataError(ErrorType.Invalid, PROOF, Keywords.VALUE);
            }


            String proofValueType = embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.TYPE);

            String encodedProofValue =  embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.VALUE);

            embeddedProof.setValue(proofValueType, encodedProofValue);

        } else {
            throw new DataError(ErrorType.Invalid, PROOF, Keywords.VALUE);
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

        return embeddedProof;
    }

    public abstract void setValue(String encoding, String value) throws DataError;

    public abstract String getValue(String encoding) throws DataError;;
    
    public JsonObject toJson() throws DataError {
        
        final JsonObjectBuilder root = 
                    Json.createObjectBuilder()
                        .add(Keywords.TYPE, Json.createArrayBuilder().add(getType()));

        if (verificationMethod != null) {
            root.add(BASE + PROOF_VERIFICATION_METHOD,
                    Json.createArrayBuilder()
                            .add(verificationMethod.toJson()));
        }

        if (created != null) {
            JsonLdUtils.setValue(root, CREATED, created);
        }

        if (purpose != null) {
            JsonLdUtils.setId(root, BASE + PROOF_PURPOSE, purpose);
        }

        if (domain != null) {
            //TODO
        }
        
        if (value != null) {
            final String proofValue = getValue(MULTIBASE_TYPE);
            
            JsonLdUtils.setValue(root, BASE + PROOF_VALUE, MULTIBASE_TYPE, proofValue);
        }
        return root.build();
    }

    /**
     * Appends the proof to the given VC/VP document. 
     * If the document has been signed already then the proof is added into a proof set.
     * 
     * @param document VC/VP document
     * @return the given VC/VP with the proof attached
     * @throws DataError 
     */
    public JsonObject addProofTo(final JsonObject document) throws DataError {

        final JsonValue proofPropertyValue = document.get(BASE + PROOF);

        final JsonArrayBuilder proofs;

        if (proofPropertyValue == null) {
            proofs  = Json.createArrayBuilder();

        } else {
            proofs = Json.createArrayBuilder(JsonUtils.toJsonArray(proofPropertyValue));
        }

        proofs.add(toJson());

        return Json.createObjectBuilder(document).add(BASE + PROOF, proofs).build();
    }
    

    @Override
    public URI getPurpose() {
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
}
