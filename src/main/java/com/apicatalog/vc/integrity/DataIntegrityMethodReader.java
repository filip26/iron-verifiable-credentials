package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class DataIntegrityMethodReader implements MethodAdapter {

    public VerificationMethod read(JsonObject document) throws DocumentError {

        LdNode node = new LdNode(document);

        URI id = node.id();
        URI type = node.type().link();
        URI controller = node.get(DataIntegrityVocab.CONTROLLER).scalar().link();

//        byte[] publicKey = document.value(DataIntegritySchema.MULTIBASE_PUB_KEY);
//        byte[] privateKey = document.value(DataIntegritySchema.MULTIBASE_PRIV_KEY);

        return new DataIntegrityKeyPair(id, type, controller,
                null, null // FIXME
        // publicKey, privateKe
        );
    }

    @Override
    public JsonObject write(VerificationMethod value) {

        LdNodeBuilder builder = new LdNodeBuilder();
        
//TODO    
        return builder.build();
    }
}
