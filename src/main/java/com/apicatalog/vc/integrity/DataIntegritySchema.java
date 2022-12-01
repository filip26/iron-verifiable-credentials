package com.apicatalog.vc.integrity;

import static com.apicatalog.vc.VcSchema.*;
import java.time.Instant;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Codec;

public class DataIntegritySchema {

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
    
    public static final LdSchema getSchema(LdTerm proofType, LdTerm verificationType) {
        return new LdSchema(
                proof(
                        type(proofType).required(),

                        property(CREATED, xsdDateTime())
                                .test(created -> Instant.now().isAfter(created))
//                        .defaultValue(Instant.now())
                                .required(),

                        property(PURPOSE, link()).required(),

                        verificationMethod(VERIFICATION_METHOD, object(
                                new DataIntegrityKeysAdapter(),
                                id().required(),
                                type(verificationType),
                                property(CONTROLLER, link()),
                                property(MULTIBASE_PUB_KEY, multibase(Algorithm.Base58Btc, Codec.Ed25519PublicKey))
                                
                                )).required(),

                        property(DOMAIN, string())
                            .test((domain, params) -> !params.containsKey("domain")
                                        || params.get("domain").equals(domain)),

                        property(CHALLENGE, string()),

                        proofValue(PROOF_VALUE, multibase(Algorithm.Base58Btc))
                                .test(key -> key.length == 64)
                                .required()
                ));
    }

}
