package com.apicatalog.vc;

import java.util.Collection;

import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;

/**
 * A generic predecessor.
 */
@Fragment(generic = true)
public interface Credential extends Verifiable {

    /**
     * Checks if the credential is expired.
     *
     * @return <code>true</code> if the credential is expired
     */
    boolean isExpired();

    /**
     * Checks if the credential is active.
     * 
     * @return <code>true</code> if the an issuance is set an is in the future
     */
    boolean isNotValidYet();

    @Term
    CredentialIssuer issuer();

    @Term
    Collection<Status> status();

    @Term
    Collection<Subject> subject();

    @Override
    default boolean isCredential() {
        return true;
    }

    @Override
    default Credential asCredential() {
        return this;
    }
}
