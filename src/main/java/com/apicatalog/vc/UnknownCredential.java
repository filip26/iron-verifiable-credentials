package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.vc.proof.Proof;

public record UnknownCredential(
//TODO        URI id,
        LinkedFragment fragment) implements Credential {

    @Override
    public Collection<Proof> proofs() {
        return Collections.emptyList();
    }

    @Override
    public boolean isExpired() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNotValidYet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LinkedFragment issuer() {
        return null;
    }

    @Override
    public Collection<LinkedFragment> status() {
        return Collections.emptyList();
    }

    @Override
    public Collection<LinkedFragment> subject() {
        return Collections.emptyList();
    }

    @Override
    public LinkedNode ld() {
        return fragment;
    }
    
    @Override
    public Collection<String> type() {
        return fragment.type();
    }
    
    @Override
    public URI id() {
        // TODO Auto-generated method stub
        return null;
    }
}
