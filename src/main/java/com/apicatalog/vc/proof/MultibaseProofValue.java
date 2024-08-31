package com.apicatalog.vc.proof;

import java.util.function.Supplier;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.LinkedTree;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public record MultibaseProofValue(
        ) implements ProofValue, LinkedLiteral {

    
    public static MultibaseProofValue of(String value, Supplier<LinkedTree> rootSupplier) {
        return new MultibaseProofValue();
    }
    
    @Override
    public String lexicalValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String datatype() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verify(CryptoSuite crypto, JsonStructure context, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError, DocumentError {
        // TODO Auto-generated method stub
        
    }

    @Override
    public byte[] toByteArray() throws DocumentError {
        // TODO Auto-generated method stub
        return null;
    }

}
