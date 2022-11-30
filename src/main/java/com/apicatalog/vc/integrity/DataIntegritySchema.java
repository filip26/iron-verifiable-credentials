package com.apicatalog.vc.integrity;

import static com.apicatalog.ld.schema.LdSchema.*;

import java.time.Instant;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.schema.LdValueAdapter;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Type;

public class DataIntegritySchema {

    static final String SEC_VOCAB = "https://w3id.org/security#";

    static final LdTerm TYPE = LdTerm.create("DataIntegrityProof", SEC_VOCAB);

    static final LdTerm CREATED = LdTerm.create("created", "http://purl.org/dc/terms/");
    static final LdTerm PURPOSE = LdTerm.create("proofPurpose", SEC_VOCAB);
    static final LdTerm VERIFICATION_METHOD = LdTerm.create("verificationMethod", SEC_VOCAB);
    static final LdTerm PROOF_VALUE = LdTerm.create("proofValue", SEC_VOCAB);
    static final LdTerm DOMAIN = LdTerm.create("domain", SEC_VOCAB);
    static final LdTerm CHALLENGE = LdTerm.create("challenge", SEC_VOCAB);

    static final LdTerm MULTIBASE_PUB_KEY = LdTerm.create("publicKeyMultibase", SEC_VOCAB);
    
    public static final LdSchema getSchema(LdTerm type) {
        return new LdSchema(
                object(
//                        type(type).required(),

                        property(CREATED, xsdDateTime())
                                .test(created -> Instant.now().isAfter(created))
//                        .defaultValue(Instant.now())
                                .required(),

//                        property(PURPOSE, reference()).required(),

                        verificationMethod(VERIFICATION_METHOD, object(
                                new DataIntegrityVerificationKeyAdapter(),
                                id(),
                                property(MULTIBASE_PUB_KEY, multibase(Algorithm.Base58Btc, Type.Key))
                                
                                )).required(),

                        property(DOMAIN, string()),

                        property(CHALLENGE, string()),

                        proofValue(PROOF_VALUE, multibase(Algorithm.Base58Btc))
                                .test(key -> key.length == 64)
                                .required()
                ));

    }

}
