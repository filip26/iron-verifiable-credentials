package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;

public record UnknownCredential(
        URI id,
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
    public IssuerDetails issuer() {
        return null;
    }

    @Override
    public Collection<Status> status() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Subject> subject() {
        return Collections.emptyList();
    }

    @Override
    public LinkedNode ld() {
        return fragment;
    }
    
    @Override
    public Collection<String> type() {
        return fragment.type().stream().toList();
    }
}
