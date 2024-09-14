package com.apicatalog.vc.primitive;

import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.literal.ByteArrayValue;
import com.apicatalog.linkedtree.literal.adapter.GenericLiteralAdapter;
import com.apicatalog.linkedtree.literal.adapter.LiteralAdapter;
import com.apicatalog.multibase.Multibase;

public record MultibaseLiteral(
        String datatype,
        String lexicalValue,
        LinkedTree ld,
        byte[] byteArrayValue) implements LinkedLiteral, ByteArrayValue {

    static final String TYPE = "https://w3id.org/security#multibase";
    
    public static MultibaseLiteral of(Multibase base, String value, LinkedTree root) {
        return new MultibaseLiteral(TYPE, value, root, base.decode(value));
    }
    
    public static String typeName() {
        return TYPE;
    }

    public static LiteralAdapter typeAdapter(final Multibase base) {
        return new GenericLiteralAdapter(
                TYPE,
                (value, root) -> MultibaseLiteral.of(base, value, root));
    }
}
