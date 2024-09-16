package com.apicatalog.vc.primitive;

import java.util.Objects;

import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.literal.ByteArrayValue;
import com.apicatalog.linkedtree.literal.adapter.DatatypeAdapter;
import com.apicatalog.linkedtree.literal.adapter.GenericDatatypeAdapter;
import com.apicatalog.multibase.Multibase;

public record MultibaseLiteral(
        String datatype,
        String lexicalValue,
        byte[] byteArrayValue,
        LinkedTree root) implements LinkedLiteral, ByteArrayValue {

    static final String TYPE = "https://w3id.org/security#multibase";

    public static MultibaseLiteral of(Multibase base, String value, LinkedTree root) {
        return new MultibaseLiteral(TYPE, value, base.decode(value), root);
    }

    public static String typeName() {
        return TYPE;
    }

    public static DatatypeAdapter typeAdapter(final Multibase base) {
        return new GenericDatatypeAdapter(
                TYPE,
                (value, root) -> MultibaseLiteral.of(base, value, root));
    }

    @Override
    public int hashCode() {
        return Objects.hash(datatype, lexicalValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MultibaseLiteral other = (MultibaseLiteral) obj;
        return Objects.equals(datatype, other.datatype) && Objects.equals(lexicalValue, other.lexicalValue);
    }

    @Override
    public String toString() {
        return "MultibaseLiteral [datatype=" + datatype + ", lexicalValue=" + lexicalValue + "]";
    }
}
