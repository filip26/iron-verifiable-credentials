package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.issuer.GenericIssuer;
import com.apicatalog.vc.status.GenericStatus;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.GenericSubject;
import com.apicatalog.vc.subject.Subject;

public abstract class VcdmCredential extends VcdmVerifiable implements Credential {

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<Subject> subject;

    protected Collection<Status> status;

    protected CredentialIssuer issuer;

    protected LinkedFragment ld;

    protected VcdmCredential() {
        // protected
    }

    protected static Credential setup(VcdmCredential credential, LinkedFragment source) throws AdapterError {

        // @id
        credential.id = source.uri();

        // subject
        credential.subject = source.collection(
                VcdmVocab.SUBJECT.uri(),
                Subject.class,
                GenericSubject::of);

        // issuer
        credential.issuer = source.fragment(
                VcdmVocab.ISSUER.uri(),
                CredentialIssuer.class,
                GenericIssuer::of);

        // status
        credential.status = source.collection(
                VcdmVocab.STATUS.uri(),
                Status.class,
                GenericStatus::new);

        credential.ld = source;
        return credential;
    }

    @Override
    public LinkedNode ld() {
        return ld;
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }

    @Override
    public CredentialIssuer issuer() {
        return issuer;
    }

    @Override
    public Collection<Status> status() {
        return status;
    }

    @Override
    public Collection<Subject> subject() {
        return subject;
    }
}
