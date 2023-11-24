package com.apicatalog.vc;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.JsonObject;

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
