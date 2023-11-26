package com.apicatalog.multikey;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.JsonObject;

public class MultiKeyAdapter implements MethodAdapter {

    @Override
    public VerificationMethod read(JsonObject document) throws DocumentError {
        return MultiKey.readMethod(document);
    }

    @Override
    public JsonObject write(VerificationMethod value) {

        LdNodeBuilder builder = new LdNodeBuilder();
        
//TODO    
        return builder.build();
    }
}
