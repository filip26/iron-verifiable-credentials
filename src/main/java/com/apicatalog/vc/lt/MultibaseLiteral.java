package com.apicatalog.vc.lt;

import java.util.function.Supplier;

import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.literal.ByteArrayValue;

public record MultibaseLiteral(
        String datatype,
        String lexicalValue,
        Supplier<LinkedTree> rootSupplier,
        byte[] byteArrayValue) implements LinkedLiteral, ByteArrayValue {

    public static final String TYPE = "https://w3id.org/security#multibase";

    @Override
    public LinkedTree root() {
        return rootSupplier.get();
    }
}
