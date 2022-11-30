package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdObjectBuilder;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.vc.assertion.Assertion;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 */
@Deprecated
public class DataIntegrityProofAdapter  {

    static final String SEC_BASE = "https://w3id.org/security#";
    
    static final String PURPOSE = "proofPurpose";
    static final String VERIFICATION_METHOD = "verificationMethod";
    static final String DOMAIN = "domain";
    static final String CHALLENGE = "challenge";
    static final String PROOF_VALUE = "proofValue";

    static final LdTerm Created = LdTerm.create("created", "http://purl.org/dc/terms/");
    static final LdTerm Purpose = LdTerm.create("proofPurpose", SEC_BASE);

    protected static final String MULTIBASE_TYPE = "https://w3id.org/security#multibase";

    protected final URI proofType;

    protected DataIntegrityProofAdapter(
                final URI proofType 
                ) {
        this.proofType = proofType;
    }

    public Assertion assertions(String doamin) {
        
//        return Assertion.thatProof(
//            type(proofType),
//            property(Created,
//                    xsdDateTime(
//                            test(created -> Instant.now().isAfter(created)),
//                            defaultValue(Instant.now())
//                            )
//                    ).required(),
//                
//            .property(Purpose)
//                .isObject()
//                .type("")
//                .property()
//                  .isString()
//                  .type()
//             
//            
//        );
//        AssertionA
//            .isType(proofType)
//            
//            .property(Purpose).isUri()
//            
//            .propertty(Created).isXsdDateTime().when(created -> Instant.now().after(created))
//            
//            .property(ProofValue).isValueObject().isType(MULTIBASE_TYPE)
//            
////            .object()
//            
//            .hasValue(Created, MULTIBASE_TYPE, created -> Instant.now().after(created))
//            .hasValue(ProofValue, MULTIBASE_TYPE)
//            
            return null;
        
    }
    
//    protected abstract byte[] decodeValue(String encoding, String value) throws DocumentError;
//
//    protected abstract String encodeValue(String encoding, byte[] value) throws DocumentError;

//    protected DataIntegrityProof read(JsonObject proofObject) throws DocumentError {
//        System.out.println(">> " + proofObject);
//
//        try {
//            // proofPurpose property
//            URI purpose = JsonLdReader.getId(proofObject, property.expand(ProofProperty.Purpose)).orElse(null);
//
//            // verificationMethod property
//            VerificationMethod verificationMethod = null;
//
//            if (proofObject.containsKey(property.expand(ProofProperty.VerificationMethod))) {
//
//                final JsonValue verificationMethodValue = proofObject
//                        .get(property.expand(ProofProperty.VerificationMethod));
//
//                if (JsonUtils.isArray(verificationMethodValue)
//                        && verificationMethodValue.asJsonArray().size() <= 0) {
//                    throw new DocumentError(ErrorType.Invalid, ProofProperty.VerificationMethod);
//                }
//
//                final JsonValue verificationMethodItem = verificationMethodValue.asJsonArray()
//                        .get(0);
//
//                //FIXME
////                if (JsonUtils.isNonEmptyObject(verificationMethodItem)) {
////                    verificationMethod = keyAdapter
////                            .deserialize(verificationMethodItem.asJsonObject());
////
////                } else {
////                    throw new DocumentError(ErrorType.Invalid, ProofProperty.VerificationMethod);
////                }
//            }
//
//            byte[] value = null;
//
//            // proofValue property
//            if (proofObject.containsKey(property.expand(ProofProperty.Value))) {
//
//                final JsonValue embeddedProofValue = proofObject
//                        .get(property.expand(ProofProperty.Value));
//
//                if (JsonUtils.isArray(embeddedProofValue)) {
//
//                    if (!embeddedProofValue.asJsonArray().stream()
//                            .allMatch(ValueObject::isValueObject)
//                            || !embeddedProofValue.asJsonArray().stream()
//                                    .map(JsonValue::asJsonObject).map(o -> o.get(Keywords.VALUE))
//                                    .allMatch(JsonUtils::isString)) {
//                        throw new DocumentError(ErrorType.Invalid, ProofProperty.Value);
//                    }
//
//                    String proofValueType = embeddedProofValue.asJsonArray().getJsonObject(0)
//                            .getString(Keywords.TYPE);
//
//                    String encodedProofValue = embeddedProofValue.asJsonArray().getJsonObject(0)
//                            .getString(Keywords.VALUE);
//
//                    value = decodeValue(proofValueType, encodedProofValue);
//
//                } else {
//                    throw new DocumentError(ErrorType.Invalid, ProofProperty.Value);
//                }
//            }
//
//            Instant created = null;
//
//            // created property
//            if (proofObject.containsKey(property.expand(ProofProperty.Created))) {
//
//                final JsonValue createdValue = proofObject
//                        .get(property.expand(ProofProperty.Created));
//
//                if (JsonUtils.isArray(createdValue)) {
//
//                    // take first created property
//                    final JsonValue createdItem = createdValue.asJsonArray().get(0);
//
//                    // expect value object and date in ISO 8601 format
//                    if (!ValueObject.isValueObject(createdItem)) {
//                        throw new DocumentError(ErrorType.Invalid, ProofProperty.Created);
//                    }
//
//                    final String createdString = ValueObject.getValue(createdItem)
//                            .filter(JsonUtils::isString).map(JsonString.class::cast)
//                            .map(JsonString::getString)
//                            .orElseThrow(() -> new DocumentError(ErrorType.Invalid,
//                                    ProofProperty.Created));
//
//                    try {
//                        OffsetDateTime createdOffset = OffsetDateTime.parse(createdString);
//
//                        created = createdOffset.toInstant();
//
//                    } catch (DateTimeParseException e) {
//                        throw new DocumentError(ErrorType.Invalid, ProofProperty.Created);
//                    }
//
//                } else {
//                    throw new DocumentError(ErrorType.Invalid, ProofProperty.Created);
//                }
//            }
//
//            final DataIntegrityProof proof = new DataIntegrityProof(proofType, purpose, verificationMethod, created, value);
//            
//            // domain property
//            if (proofObject.containsKey(property.expand(ProofProperty.Domain))) {
//                proof.setDomain(ValueObject
//                        .getValue(proofObject.get(property.expand(ProofProperty.Domain))
//                                .asJsonArray().get(0))
//                        .filter(JsonUtils::isString).map(JsonString.class::cast)
//                        .map(JsonString::getString).orElseThrow(
//                                () -> new DocumentError(ErrorType.Invalid, ProofProperty.Domain))
//                        );
//            }
//
//            // domain property
//            if (proofObject.containsKey(property.expand(ProofProperty.Domain))) {
//                proof.setDomain(ValueObject
//                        .getValue(proofObject.get(property.expand(ProofProperty.Domain))
//                                .asJsonArray().get(0))
//                        .filter(JsonUtils::isString).map(JsonString.class::cast)
//                        .map(JsonString::getString).orElseThrow(
//                                () -> new DocumentError(ErrorType.Invalid, ProofProperty.Domain))
//                        );
//            }
//
//            // challenge property
//            if (proofObject.containsKey(property.expand(ProofProperty.Challenge))) {
//                proof.setDomain(ValueObject
//                        .getValue(proofObject.get(property.expand(ProofProperty.Challenge))
//                                .asJsonArray().get(0))
//                        .filter(JsonUtils::isString).map(JsonString.class::cast)
//                        .map(JsonString::getString).orElseThrow(
//                                () -> new DocumentError(ErrorType.Invalid, ProofProperty.Challenge))
//                        );
//            }
//
//            
//            return proof;
//            
//        } catch (InvalidJsonLdValue e) {
//            e.printStackTrace();            
//        }
//        //tODO
//        return null;
//    }

    protected JsonLdObjectBuilder write(final JsonLdObjectBuilder builder, final DataIntegrityProof proof) throws DocumentError {
        
        builder.setType(proof.getType());
        
        //FIXME
//        if (proof.getMethod() != null) {
//            builder.add(property.expand(ProofProperty.VerificationMethod), Json.createArrayBuilder()
//                    .add(keyAdapter.serialize(proof.getMethod())));
//        }

        if (proof.getCreated() != null) {
            builder.vocab("http://purl.org/dc/terms/");
//            builder.add(CREATED, proof.getCreated());
        }

        builder.vocab("https://w3id.org/security#");
        
        if (proof.getPurpose() != null) {
            builder.addReference(PURPOSE, proof.getPurpose());
        }

        if (proof.getDomain() != null) {
            builder.add(DOMAIN, proof.getDomain());
        }
        
        if (proof.getChallenge() != null) {
            builder.add(CHALLENGE, proof.getChallenge());
        }

        if (proof.getValue() != null) {
      //      final String proofValue = encodeValue(MULTIBASE_TYPE, proof.getValue());
        //    builder.add(PROOF_VALUE, MULTIBASE_TYPE, proofValue);
        }

        return builder;
    }

//    @Override
    public JsonObject setProofValue(final JsonObject proof, final byte[] value)
            throws DocumentError {

  //      final String proofValue = encodeValue(MULTIBASE_TYPE, value);

        return new JsonLdObjectBuilder(proof)
                    .vocab(SEC_BASE)
    //                .add(PROOF_VALUE, MULTIBASE_TYPE, proofValue)
                    .build();
    }

    public JsonObject removeProofValue(final JsonObject proof) {
        return Json.createObjectBuilder(proof).remove(SEC_BASE + PROOF_VALUE).build();
    }

//    @Override
    public URI type() {
        return proofType;
    }

//    @Override
    public JsonObject serialize(DataIntegrityProof proof) throws DocumentError {
        return write(new JsonLdObjectBuilder(), proof).build();
    }

//    public DataIntegrityProof deserialize(JsonObject object) throws DocumentError {
//        if (object == null) {
//            throw new IllegalArgumentException("Parameter 'object' must not be null.");
//        }
//
//        // data integrity checks
//        if (JsonUtils.isNotObject(object)) {
//            throw new DocumentError(ErrorType.Invalid, "Proof");
//        }
//
//        final JsonObject proofObject = object.asJsonObject();
//
//        if (!JsonLdReader.isTypeOf(proofType.toString(), proofObject)) {
//
//            // @type property
//            if (!JsonLdReader.hasType(proofObject)) {
//                throw new DocumentError(ErrorType.Missing, "ProofType");
//            }
//
//            throw new DocumentError(ErrorType.Unknown, "CryptoSuiteType");
//        }
//        return read(proofObject);
//    }
}
