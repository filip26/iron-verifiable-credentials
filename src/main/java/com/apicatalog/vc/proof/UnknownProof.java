package com.apicatalog.vc.proof;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.oxygen.ld.LinkedData;
import com.apicatalog.oxygen.ld.LinkedNode;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class UnknownProof 
//extends LinkedObject
implements Proof {

    protected UnknownProof(JsonObject expanded) {
//        super(expanded);
    }

    @Override
    public VerificationMethod method() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProofValue signature() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI previousProof() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CryptoSuite cryptoSuite() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void verify(JsonStructure context, JsonObject data, VerificationKey method) throws VerificationError, DocumentError {
        throw new VerificationError(Code.UnsupportedCryptoSuite);
    }

    @Override
    public MethodAdapter methodProcessor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI id() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> type() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> terms() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<LinkedData> values(String term) {
        // TODO Auto-generated method stub
        return null;
    }

}
