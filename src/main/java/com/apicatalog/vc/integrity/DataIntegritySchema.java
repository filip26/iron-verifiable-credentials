package com.apicatalog.vc.integrity;

import static com.apicatalog.ld.schema.LdSchema.*;

import java.time.Instant;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;

public class DataIntegritySchema {

    static final String SEC_VOCAB = "https://w3id.org/security#";

    static final LdTerm TYPE = LdTerm.create("DataIntegrityProof", SEC_VOCAB);

    static final LdTerm CREATED = LdTerm.create("created", "http://purl.org/dc/terms/");
    static final LdTerm PURPOSE = LdTerm.create("proofPurpose", SEC_VOCAB);
    static final LdTerm VERIFICATION_METHOD = LdTerm.create("verificationMethod", SEC_VOCAB);
    static final LdTerm PROOF_VALUE = LdTerm.create("proofValue", SEC_VOCAB);
    static final LdTerm DOMAIN = LdTerm.create("domain", SEC_VOCAB);
    static final LdTerm CHALLENGE = LdTerm.create("challenge", SEC_VOCAB);

    public static final LdSchema getSchema(LdTerm type) {
        return new LdSchema(
                object(
                        type(type).required(),

                        property(CREATED, xsdDateTime())
                                .test(created -> Instant.now().isAfter(created))
//                        .defaultValue(Instant.now())
                                .required(),

                        property(PURPOSE, reference()).required(),

//                        verificationMethod(VERIFICATION_METHOD, new Test).required(),

                        property(DOMAIN, string()),

                        property(CHALLENGE, value(string())),

                        proofValue(PROOF_VALUE, multibase())
                                .test(key -> key.length == 64)
                                .required()
                ));

    }

}
