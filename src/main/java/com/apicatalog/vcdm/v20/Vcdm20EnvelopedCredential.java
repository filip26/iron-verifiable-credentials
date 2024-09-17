package com.apicatalog.vcdm.v20;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;

public class Vcdm20EnvelopedCredential implements Credential {

    protected URI id;
    
    protected LinkedFragment ld;
    
    protected Vcdm20EnvelopedCredential() {
        // protected
    }
    
    public static Credential of(LinkedFragment source) throws NodeAdapterError {
        var credential = new Vcdm20EnvelopedCredential();

        // @id
        credential.id = source.uri();

        
        credential.ld = source;
        
        return credential;
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public Collection<String> type() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Proof> proofs() {
        return Collections.emptyList();
    }

    @Override
    public boolean isExpired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNotValidYet() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public CredentialIssuer issuer() {
        // TODO Auto-generated method stub
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
        return ld;
    }
}
