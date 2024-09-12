package com.apicatalog.vc.lt;

import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.literal.ByteArrayValue;

public record MultibaseLiteral(
        String datatype,
        String lexicalValue,
        LinkedTree root,
        byte[] byteArrayValue) implements LinkedLiteral, ByteArrayValue {

    static final String TYPE = "https://w3id.org/security#multibase";
    
    public static String typeName() {
        return TYPE;
    }
    

}
