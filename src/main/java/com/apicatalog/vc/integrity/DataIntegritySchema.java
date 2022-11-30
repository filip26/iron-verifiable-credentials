package com.apicatalog.vc.integrity;

import static com.apicatalog.ld.schema.LdSchema.multibase;
import static com.apicatalog.ld.schema.LdSchema.object;
import static com.apicatalog.ld.schema.LdSchema.proofValue;
import static com.apicatalog.ld.schema.LdSchema.property;
import static com.apicatalog.ld.schema.LdSchema.reference;
import static com.apicatalog.ld.schema.LdSchema.string;
import static com.apicatalog.ld.schema.LdSchema.type;
import static com.apicatalog.ld.schema.LdSchema.*;
import static com.apicatalog.ld.schema.LdSchema.xsdDateTime;

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

    public static void main(String[] args) {

        var schema = 
                object(
                    type(TYPE).required(),
                    
                    property(CREATED, xsdDateTime())
                        .test(created -> Instant.now().isAfter(created))
//                        .defaultValue(Instant.now())
                        .required(),

                    property(PURPOSE, reference()).required(),
                    
                    verificationMethod(VERIFICATION_METHOD, multibase() 
                            ).required(),
                    
                    property(DOMAIN, string()),
                    
                    property(CHALLENGE, value(string())),
                    
                    proofValue(PROOF_VALUE, multibase())
                        .test(key -> key.length == 64)
                        .required()
                    
        );
        
//        Ld.get(CREATED, Json.createObjectBuilder().add(CREATED.id(), 
//                Json.createArrayBuilder()
//                .add(Json.createObjectBuilder().add(Keywords.VALUE, "AAAAAAA").add(Keywords.TYPE,"http://www.w3.org/2001/XMLSchema#dateTime"))
//                    ).build()
//                );
//        
//        schema.read(Json.createArrayBuilder().build());
    }

}
