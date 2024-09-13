package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;

public record GenericCredential(
        URI id,
        LinkedNode ld) implements Credential {

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
    public CredentialIssuer issuer() {
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
    public Collection<String> type() {
        return ld().asFragment().type().stream().toList();
    }

    @Override
    public void proofs(Collection<Proof> proofs) {
        throw new UnsupportedOperationException();
    }
}
