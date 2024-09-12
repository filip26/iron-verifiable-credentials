package com.apicatalog.vc.proof;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;

public record UnknownProof(
//TODO        URI id,
        LinkedFragment fragment) implements Proof {

    @Override
    public VerificationMethod method() {
        return null;
    }

    @Override
    public ProofValue signature() {
        return null;
    }

    @Override
    public URI previousProof() {
        return null;
    }

    @Override
    public CryptoSuite cryptoSuite() {
        return null;
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        throw new UnsupportedOperationException("An uknown proof cannot be validated.");
    }

//    @Override
//    public MethodAdapter methodProcessor() {
//        return null;
//    }

    @Override
    public void verify(VerificationKey method) throws VerificationError, DocumentError {
        throw new VerificationError(Code.UnsupportedCryptoSuite);   
    }
    
    @Override
    public LinkedNode ld() {
        return fragment;
    }
    
    @Override
    public Collection<String> type() {
        return fragment.type().stream().toList();
    }
    
    @Override
    public URI id() {
        return null;
    }
}
