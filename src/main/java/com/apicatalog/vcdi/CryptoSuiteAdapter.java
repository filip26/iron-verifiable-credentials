package com.apicatalog.vcdi;

import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.literal.adapter.DataTypeAdapter;
import com.apicatalog.linkedtree.literal.adapter.DataTypeNormalizer;
import com.apicatalog.linkedtree.primitive.TypedLiteral;

public class CryptoSuiteAdapter implements DataTypeAdapter, DataTypeNormalizer<CryptoSuite> {

    @Override
    public LinkedLiteral materialize(String source) throws NodeAdapterError {
        throw new UnsupportedOperationException();
    }

    @Override
    public String datatype() {
        return "https://w3id.org/security#cryptosuiteString";
    }

    @Override
    public Class<? extends LinkedLiteral> typeInterface() {
        return TypedLiteral.class;
    }

    @Override
    public String normalize(CryptoSuite value) {
        return value.id();
    }

}
