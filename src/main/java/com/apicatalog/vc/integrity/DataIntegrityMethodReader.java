package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.signature.VerificationMethod;

import jakarta.json.JsonObject;

public class DataIntegrityMethodReader {

    public static VerificationMethod read(JsonObject document) throws DocumentError {

        LdNode node = new LdNode(document);

        URI id = node.id();
        URI type = node.get(LdTerm.TYPE).scalar().link();
        URI controller = node.get(DataIntegrityVocab.CONTROLLER).scalar().link();

//        byte[] publicKey = document.value(DataIntegritySchema.MULTIBASE_PUB_KEY);
//        byte[] privateKey = document.value(DataIntegritySchema.MULTIBASE_PRIV_KEY);

        return new DataIntegrityKeyPair(id, type, controller,
                null, null // FIXME
        // publicKey, privateKe
        );
    }
}
