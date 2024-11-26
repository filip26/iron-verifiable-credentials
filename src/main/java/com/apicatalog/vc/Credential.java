package com.apicatalog.vc;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vcdm.VcdmVocab;

/**
 * A generic verifiable credential.
 */
@Fragment(generic = true)
@Vocab("https://www.w3.org/2018/credentials#")
public interface Credential extends VerifiableDocument {

    /**
     * Check if the credential is expired.
     *
     * @return <code>true</code> if the credential is expired
     */
    boolean isExpired();

    /**
     * Check if the credential is active.
     * 
     * @return <code>true</code> if the an issuance is set an is in the future
     */
    boolean isNotValidYet();

    /**
     * Information about an issuer.
     * 
     * @return an issuer instance or <code>null</code>.
     */
    @Term
    CredentialIssuer issuer();

    @Term("credentialStatus")
    Collection<Status> status();

    @Term("credentialSubject")
    Collection<Subject> subject();

    @Term
    Collection<Evidence> evidence();

    @Term
    Collection<TermsOfUse> termsOfUse();
    
    @Term
    Collection<RefreshService> refreshService();
    
    @Term("credentialSchema")
    Collection<CredentialSchema> schema();

    
    @Override
    default boolean isCredential() {
        return true;
    }

    @Override
    default Credential asCredential() {
        return this;
    }
    
    @Override
    default void validate() throws DocumentError {
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }
        
        if (issuer() != null) {
            issuer().validate();
        }
        
        if (subject() == null || subject().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.SUBJECT);
        }
        
        for (Subject subject : subject()) {
            subject.validate();
        }
        
        if (status() != null && !status().isEmpty()) {
            for (Status status : status()) {
                status.validate();
            }
        }
    }
}
