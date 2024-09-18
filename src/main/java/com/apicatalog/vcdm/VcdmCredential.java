package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.issuer.GenericIssuer;
import com.apicatalog.vc.model.Evidence;
import com.apicatalog.vc.model.GenericEvidence;
import com.apicatalog.vc.model.GenericRefreshService;
import com.apicatalog.vc.model.GenericTermsOfUse;
import com.apicatalog.vc.model.RefreshService;
import com.apicatalog.vc.model.TermsOfUse;
import com.apicatalog.vc.status.GenericStatus;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.GenericSubject;
import com.apicatalog.vc.subject.Subject;

public abstract class VcdmCredential extends VcdmVerifiable implements Credential {

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<Subject> subject;

    protected Collection<Status> status;

    protected CredentialIssuer issuer;

    protected TermsOfUse termsOfUse;
    
    protected Evidence evidence;
    
    protected RefreshService refreshService;
    
    protected LinkedFragment ld;

    protected VcdmCredential() {
        // protected
    }

    protected static Credential setup(VcdmCredential credential, LinkedFragment source) throws NodeAdapterError {

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

        credential.evidence = source.fragment(
                VcdmVocab.EVIDENCE.uri(), 
                Evidence.class, 
                GenericEvidence::of);

        credential.termsOfUse = source.fragment(
                VcdmVocab.TERMS_OF_USE.uri(), 
                TermsOfUse.class, 
                GenericTermsOfUse::of);
        
        credential.refreshService = source.fragment(
                VcdmVocab.REFRESH_SERVICE.uri(), 
                RefreshService.class, 
                GenericRefreshService::of);
        
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
    
    public Evidence evidence() {
        return evidence;
    }

    public TermsOfUse termsOfUse() {
        return termsOfUse;
    }
    
    public RefreshService refreshService() {
        return refreshService;
    }
}
