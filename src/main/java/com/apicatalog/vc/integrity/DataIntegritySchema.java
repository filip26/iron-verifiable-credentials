package com.apicatalog.vc.integrity;

import static com.apicatalog.jsonld.schema.LdSchema.id;
import static com.apicatalog.jsonld.schema.LdSchema.link;
import static com.apicatalog.jsonld.schema.LdSchema.multibase;
import static com.apicatalog.jsonld.schema.LdSchema.object;
import static com.apicatalog.jsonld.schema.LdSchema.property;
import static com.apicatalog.jsonld.schema.LdSchema.string;
import static com.apicatalog.jsonld.schema.LdSchema.type;
import static com.apicatalog.jsonld.schema.LdSchema.xsdDateTime;

import java.net.URI;
import java.time.Instant;
import java.util.function.Predicate;

import com.apicatalog.jsonld.schema.LdProperty;
import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Codec;

public final class DataIntegritySchema {

    public static final String SEC_VOCAB = "https://w3id.org/security#";

    public static final LdTerm CREATED = LdTerm.create("created", "http://purl.org/dc/terms/");
    public static final LdTerm PURPOSE = LdTerm.create("proofPurpose", SEC_VOCAB);
    public static final LdTerm VERIFICATION_METHOD = LdTerm.create("verificationMethod", SEC_VOCAB);
    public static final LdTerm PROOF_VALUE = LdTerm.create("proofValue", SEC_VOCAB);
    public static final LdTerm DOMAIN = LdTerm.create("domain", SEC_VOCAB);
    public static final LdTerm CHALLENGE = LdTerm.create("challenge", SEC_VOCAB);

    public static final LdTerm CRYPTO_SUITE = LdTerm.create("cryptosuite", SEC_VOCAB);

    public static final LdTerm PREVIOUS_PROOF = LdTerm.create("previousProof", SEC_VOCAB);

    public static final LdTerm CONTROLLER = LdTerm.create("controller", SEC_VOCAB);
    public static final LdTerm MULTIBASE_PUB_KEY = LdTerm.create("publicKeyMultibase", SEC_VOCAB);
    public static final LdTerm MULTIBASE_PRIV_KEY = LdTerm.create("privateKeyMultibase", SEC_VOCAB);

    private DataIntegritySchema() {
        /* protected */ }

    public static final LdProperty<byte[]> getPublicKey(Algorithm encoding, Codec codec, Predicate<byte[]> predicate) {
        return property(MULTIBASE_PUB_KEY, multibase(encoding, codec)).test(predicate);
    }

    public static final LdProperty<byte[]> getPrivateKey(Algorithm encoding, Codec codec, Predicate<byte[]> predicate) {
        return property(MULTIBASE_PRIV_KEY, multibase(encoding, codec)).test(predicate);
    }

    public static final LdProperty<byte[]> getProofValue(Algorithm encoding, Predicate<byte[]> predicate) {
        return property(PROOF_VALUE, multibase(encoding)).test(predicate);
    }

    public static final LdProperty<URI> getMethod() {
        return property(PROOF_VALUE, link());
    }

    public static final LdProperty<VerificationMethod> getEmbeddedMethod(LdSchema method) {
        return property(VERIFICATION_METHOD, method.map(new DataIntegrityKeysAdapter()));
    }

    public static final LdSchema getKeyPair(LdTerm verificationType, LdProperty<byte[]> publicKey, LdProperty<byte[]> privateKey) {
        return new LdSchema(
                object(
                        id().required(),
                        type(verificationType),
                        property(CONTROLLER, link()),
                        publicKey,
                        privateKey));
    }

    public static final LdSchema getVerificationKey(LdTerm verificationType, LdProperty<byte[]> publicKey) {
        return new LdSchema(
                object(
                        id().required(),
                        type(verificationType),
                        property(CONTROLLER, link()),
                        publicKey));
    }

    public static final LdSchema getProof(LdTerm proofType, LdProperty<?> method, LdProperty<byte[]> proofValue) {
        return new LdSchema(object(
                type(proofType).required(),

                property(CREATED, xsdDateTime())
                        .test(created -> Instant.now().isAfter(created))
//                        .defaultValue(Instant.now())
                        .required(),

                property(PURPOSE, link()).required()
                        .test((purpose, params) -> !params.containsKey(DataIntegritySchema.PURPOSE.name())
                                || purpose != null && params.get(DataIntegritySchema.PURPOSE.name()).toString().equals(purpose.toString())),

//                property(VERIFICATION_METHOD, ).required(),

                property(DOMAIN, string())
                        .test((domain, params) -> !params.containsKey(DataIntegritySchema.DOMAIN.name())
                                || params.get(DataIntegritySchema.DOMAIN.name()).equals(domain)),

                property(CRYPTO_SUITE, string()),

                property(CHALLENGE, string())
                        .test((challenge, params) -> !params.containsKey(DataIntegritySchema.CHALLENGE.name())
                                || params.get(DataIntegritySchema.CHALLENGE.name()).equals(challenge)),

                method.required(),
                proofValue.required()));
    }
}
