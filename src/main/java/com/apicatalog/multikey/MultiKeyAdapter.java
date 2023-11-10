package com.apicatalog.multikey;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.integrity.DataIntegrityKeyPair;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class MultiKeyAdapter implements MethodAdapter {

    @Override
    public VerificationMethod read(JsonObject document) throws DocumentError {

        LdNode node = new LdNode(document);


//FIXME        URI controller = node.get(DataIntegrityVocab.CONTROLLER).scalar().link();

//        byte[] publicKey = document.value(DataIntegritySchema.MULTIBASE_PUB_KEY);
//        byte[] privateKey = document.value(DataIntegritySchema.MULTIBASE_PRIV_KEY);

        MultiKey multikey = new MultiKey();
        multikey.id = node.id();
//        multikey.publicKey = node.get(MultiKey.PUBLIC_KEY).scalar().multibase()
//        MultiKey.type = node.type().link();
        
        return multikey;
        
//        return new DataIntegrityKeyPair(id, type, null,
//                null, null // FIXME
        // publicKey, privateKe
//        );
    }

    @Override
    public JsonObject write(VerificationMethod value) {

        LdNodeBuilder builder = new LdNodeBuilder();
        
//TODO    
        return builder.build();
    }
}
