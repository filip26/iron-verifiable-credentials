package com.apicatalog.vc.integrity;

import static com.apicatalog.ld.schema.LdSchema.id;
import static com.apicatalog.ld.schema.LdSchema.link;
import static com.apicatalog.ld.schema.LdSchema.multibase;
import static com.apicatalog.ld.schema.LdSchema.object;
import static com.apicatalog.ld.schema.LdSchema.property;
import static com.apicatalog.ld.schema.LdSchema.string;
import static com.apicatalog.ld.schema.LdSchema.type;
import static com.apicatalog.ld.schema.LdSchema.xsdDateTime;
import static com.apicatalog.vc.VcSchema.proof;
import static com.apicatalog.vc.VcSchema.proofValue;
import static com.apicatalog.vc.VcSchema.verificationMethod;

import java.time.Instant;
import java.util.function.Predicate;

import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Codec;

public final class DataIntegrity {

    public static final String SEC_VOCAB = "https://w3id.org/security#";

    public static final LdTerm TYPE = LdTerm.create("DataIntegrityProof", SEC_VOCAB);

    public static final LdTerm CREATED = LdTerm.create("created", "http://purl.org/dc/terms/");
    public static final LdTerm PURPOSE = LdTerm.create("proofPurpose", SEC_VOCAB);
    public static final LdTerm VERIFICATION_METHOD = LdTerm.create("verificationMethod", SEC_VOCAB);
    public static final LdTerm PROOF_VALUE = LdTerm.create("proofValue", SEC_VOCAB);
    public static final LdTerm DOMAIN = LdTerm.create("domain", SEC_VOCAB);
    public static final LdTerm CHALLENGE = LdTerm.create("challenge", SEC_VOCAB);

    public static final LdTerm CONTROLLER = LdTerm.create("controller", SEC_VOCAB);
    public static final LdTerm MULTIBASE_PUB_KEY = LdTerm.create("publicKeyMultibase", SEC_VOCAB);
    public static final LdTerm MULTIBASE_PRIV_KEY = LdTerm.create("privateKeyMultibase", SEC_VOCAB);

    private DataIntegrity() { /* protected */ }

    public static final LdProperty<byte[]> getPublicKey(Algorithm encoding, Codec codec, Predicate<byte[]> predicate) {
        return property(MULTIBASE_PUB_KEY, multibase(encoding, codec)).test(predicate);
    }

    public static final LdProperty<byte[]> getPrivateKey(Algorithm encoding, Codec codec, Predicate<byte[]> predicate) {
        return property(MULTIBASE_PRIV_KEY, multibase(encoding, codec)).test(predicate);
    }

    public static final LdSchema getKeyPair(LdTerm verificationType, LdProperty<byte[]> publicKey, LdProperty<byte[]> privateKey) {
        return new LdSchema(
                    object(
                        id().required(),
                        type(verificationType),
                        property(CONTROLLER, link()),
                        publicKey,
                        privateKey
                        ));
    }

    public static final LdSchema getVerificationKey(LdTerm verificationType, LdProperty<byte[]> publicKey) {
        return new LdSchema(
                    object(
                        id().required(),
                        type(verificationType),
                        property(CONTROLLER, link()),
                        publicKey
                        ));
    }

    public static final LdSchema getProof(LdTerm proofType, Algorithm proofValueEncoding, Predicate<byte[]> proofValuePredicate, LdSchema method) {
        return proof(
                type(proofType).required(),

                property(CREATED, xsdDateTime())
                        .test(created -> Instant.now().isAfter(created))
//                        .defaultValue(Instant.now())
                        .required(),

                property(PURPOSE, link()).required(),

                verificationMethod(VERIFICATION_METHOD,
                        method.map(new DataIntegrityKeysAdapter())).required(),

                property(DOMAIN, string())
                        .test((domain, params) -> !params.containsKey(DataIntegrity.DOMAIN.name())
                                || params.get(DataIntegrity.DOMAIN.name()).equals(domain)),

                property(CHALLENGE, string()),

                proofValue(PROOF_VALUE, multibase(proofValueEncoding))
                        .test(proofValuePredicate)
                        .required());
    }
}
