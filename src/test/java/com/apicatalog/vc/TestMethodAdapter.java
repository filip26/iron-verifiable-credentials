package com.apicatalog.vc;

import static com.apicatalog.jsonld.schema.LdSchema.id;
import static com.apicatalog.jsonld.schema.LdSchema.link;
import static com.apicatalog.jsonld.schema.LdSchema.multibase;
import static com.apicatalog.jsonld.schema.LdSchema.object;
import static com.apicatalog.jsonld.schema.LdSchema.property;
import static com.apicatalog.jsonld.schema.LdSchema.type;

import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.jsonld.schema.adapter.LdValueAdapter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.MulticodecRegistry;
import com.apicatalog.vc.integrity.DataIntegrityKeysAdapter;
import com.apicatalog.vc.integrity.DataIntegrityMethodReader;
import com.apicatalog.vc.integrity.DataIntegritySchema;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class TestMethodAdapter implements MethodAdapter {


    @Override
    public JsonObject write(VerificationMethod value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VerificationMethod read(JsonObject expanded) throws DocumentError {

//        return DataIntegrityMethodReader.read(expanded);
        return null;
    }

//    LdValueAdapter<JsonValue, VerificationMethod> adapter = object(
//            id(),
//            type(LdTerm.create("TestVerificationKey2022", "https://w3id.org/security#")),
//            property(DataIntegritySchema.CONTROLLER, link()),
//            property(DataIntegritySchema.MULTIBASE_PUB_KEY, multibase(Algorithm.Base58Btc, MulticodecRegistry.ED25519_PUBLIC_KEY)),
//            property(DataIntegritySchema.MULTIBASE_PRIV_KEY, multibase(Algorithm.Base58Btc, MulticodecRegistry.ED25519_PRIVATE_KEY))
//        ).map(new DataIntegrityKeysAdapter());

//    
//    static VerificationMethod read(JsonObject document) throws DocumentError {
//        return DataIntegrityMethodReader.read(document);
//    }
    
}
