package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.issuer.GenericIssuer;
import com.apicatalog.vc.model.CredentialSchema;
import com.apicatalog.vc.model.Evidence;
import com.apicatalog.vc.model.GenericCredentialSchema;
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

    protected Collection<TermsOfUse> termsOfUse;
    
    protected Collection<Evidence> evidence;
    
    protected Collection<RefreshService> refreshService;
    
    protected Collection<CredentialSchema> schema;
    
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
                GenericStatus::of);

        credential.evidence = source.collection(
                VcdmVocab.EVIDENCE.uri(), 
                Evidence.class, 
                GenericEvidence::of);

        credential.termsOfUse = source.collection(
                VcdmVocab.TERMS_OF_USE.uri(), 
                TermsOfUse.class, 
                GenericTermsOfUse::of);
        
        credential.refreshService = source.collection(
                VcdmVocab.REFRESH_SERVICE.uri(), 
                RefreshService.class, 
                GenericRefreshService::of);

        credential.schema = source.collection(
                VcdmVocab.CREDENTIAL_SCHEMA.uri(), 
                CredentialSchema.class, 
                GenericCredentialSchema::of);

        credential.ld = source;
        return credential;
    }

    @Override
    public void validate() throws DocumentError {

        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }

        // subject - mandatory
        if (subject() == null || subject().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.SUBJECT);
        }

        // issuer
        if (issuer() == null) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUER);
        }
        if (issuer().id() == null) {
            throw new DocumentError(ErrorType.Missing, "IssuerId");
        }
        
        issuer.validate();

        // status
        if (status() != null) {
            for (final Status item : status()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "StatusType");
                }
                item.validate();
            }
        }

        if (termsOfUse() != null) {
            for (final TermsOfUse item : termsOfUse()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "TermsOfUseType");
                }
                item.validate();
            }
        }

        if (evidence() != null) {
            for (final Evidence item : evidence()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "EvidenceType");
                }
                item.validate();
            }
        }

        if (refreshService() != null) {
            for (final RefreshService item : refreshService()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "RefreshServiceType");
                }
                item.validate();
            }
        }

        if (schema() != null) {
            for (final CredentialSchema item : schema()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "CredentialSchemaType");
                }
            }
        }        
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
    
    public Collection<Evidence> evidence() {
        return evidence;
    }

    public Collection<TermsOfUse> termsOfUse() {
        return termsOfUse;
    }
    
    public Collection<RefreshService> refreshService() {
        return refreshService;
    }
    
    public Collection<CredentialSchema> schema() {
        return schema;
    }
}
