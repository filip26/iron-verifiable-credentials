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
        String datatype,
        String lexicalValue, 
        Supplier<LinkedTree> rootSupplier, 
        byte[] toByteArray
        ) implements ProofValue, LinkedLiteral {
    
    @Override
    public void verify(CryptoSuite crypto, JsonStructure context, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError, DocumentError {
        // TODO Auto-generated method stub    
    }

    @Override
    public LinkedTree root() {
        return rootSupplier.get();
    }

}
