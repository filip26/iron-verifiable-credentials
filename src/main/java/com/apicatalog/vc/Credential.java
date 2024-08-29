package com.apicatalog.vc;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;

/**
 * A generic predecessor.
 */
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

    LinkedFragment issuer();

    Collection<Status> status();

    Collection<Subject> claims();

    @Override
    default void validate() throws DocumentError {
        throw new UnsupportedOperationException();
    }
////    
////    {
//        
//        // @type - mandatory
//        if (type() == null || type().isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
//        }
//
//        // subject - mandatory
//        if (claims() == null || claims().isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
//        }
//        for (Subject item : claims()) {
//            item.validate();
//        }
//
//        // issuer
//        if (issuer() == null) {
//            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
//        }
//        issuer().validate();
//
//        // status
//        if (status() != null) {
//            for (final Status item : status()) {
//                item.validate();
//            }
//        }
//
//        // v1
//        if ((version() == null || ModelVersion.V11.equals(version()))
//                && issuanceDate() == null) {
//            // issuance date is a mandatory property
//            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUANCE_DATE);
//        }
//
//        // model v1
//        if ((issuanceDate() != null
//                && expiration() != null
//                && issuanceDate().isAfter(expiration()))
//                // model v2
//                || (validFrom() != null
//                        && validUntil() != null
//                        && validFrom().isAfter(validUntil()))) {
//            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
//        }
//    }

    @Override
    default boolean isCredential() {
        return true;
    }

    @Override
    default Credential asCredential() {
        return this;
    }
}
