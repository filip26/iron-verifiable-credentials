package com.apicatalog.vc.proof;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.controller.method.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.selector.InvalidSelector;

public record GenericProof(
        URI id,
        LinkedNode ld) implements Proof {

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
        throw new UnsupportedOperationException("An unknown proof cannot be validated " + type() + ".");
    }

    @Override
    public void verify(VerificationKey method) throws VerificationError, DocumentError {   
        throw new UnsupportedOperationException("An unknown proof cannot be verified " + type() + ".");
    }
    
    @Override
    public Collection<String> type() {
        return ld().asFragment().type().stream().toList();
    }
    
    @Override
    public URI id() {
        return id;
    }
    
    public static GenericProof of(LinkedNode source) throws InvalidSelector {
        return new GenericProof(source.asFragment().uri(), source);
    }
}
