package com.apicatalog.vcdi;

import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.literal.ImmutableLiteral;
import com.apicatalog.linkedtree.orm.mapper.ObjectMapper;

public class CryptoSuiteMapper implements ObjectMapper<LinkedLiteral, CryptoSuite> {

    public static String DATATYPE = "https://w3id.org/security#cryptosuiteString";

//    @Override
//    public String datatype() {
//        return ;
//    }

//    @Override
//    public LinkedLiteral literal(CryptoSuite value) {
//        return value.id();
//    }

    @Override
    public CryptoSuite object(LinkedLiteral literal) throws NodeAdapterError {

        if (DATATYPE.equals(literal.datatype())) {
            throw new NodeAdapterError("Expected " + DATATYPE + " but got " + literal);
        }

        return null; // TODO this is interesting, might be a way how to inject a suite
    }

    @Override
    public LinkedLiteral literal(CryptoSuite object) {
        return new ImmutableLiteral(object.name(), DATATYPE);
    }

}
