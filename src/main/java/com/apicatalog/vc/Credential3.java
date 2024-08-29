package com.apicatalog.vc;

import java.time.Instant;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVersion;

/**
 * Represents a verifiable credential (VC).
 *
 * @see <a href= "https://www.w3.org/TR/vc-data-model/#credentials">v1.1</a>
 * @see <a href= "https://w3c.github.io/vc-data-model/#credentials">v2.0</a>
 * 
 * @since 0.9.0
 */
public interface Credential3  {

    /**
     * A date time when the credential has been issued. VC data model v1.1.
     * Deprecated in favor of {@link Credential3#validFrom()} by VC data model v2.0.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @since 0.8.1
     * 
     * @return a date time from which the credential claims are valid or
     *         <code>null</code>.
     */
    Instant issuanceDate();

    /**
     * VC data model v1.1 only. Deprecated in favor of
     * {@link Credential3#validUntil()} by VC data model v2.0.
     * 
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    Instant expiration();

    /**
     * A date time from the credential is valid. VC data model v2.0.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @since 0.8.1
     * 
     * @return a date time
     */
    Instant validFrom();

    /**
     * The date and time the credential ceases to be valid, which could be a date
     * and time in the past. Note that this value represents the latest point in
     * time at which the information associated with the credentialSubject property
     * is valid. VC data model version 2.0.
     * 
     * @return the date and time the credential ceases to be valid
     */
    Instant validUntil();

    /**
     * Checks if the credential is expired.
     *
     * @return <code>true</code> if the credential is expired
     */
    default boolean isExpired() {
        return (expiration() != null && expiration().isBefore(Instant.now()))
                || (validUntil() != null && validUntil().isBefore(Instant.now()));
    }

    /**
     * Checks if the credential is active, i.e. does not define validFrom property
     * or the property datetime is before now.
     * 
     * @since 0.9.0
     * 
     * @return <code>true</code> if the credential is active
     */
    default boolean isNotValidYet() {
        return (issuanceDate() != null && issuanceDate().isAfter(Instant.now()))
                || (validFrom() != null && validFrom().isAfter(Instant.now()));
    }

    /**
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuer">Issuerr</a>
     * @return {@link IssuerDetails} representing the issuer in an expanded form
     */
    IssuerDetails issuer();

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
     * 
     * @return
     */
    Collection<Status> status();

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#credential-subject">Credential
     *      Subject</a>
     * 
     * @return
     */
    Collection<Subject> subject();

//    @Override
//    default void validate() throws DocumentError {
//        
//        // @type - mandatory
//        if (type() == null || type().isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
//        }
//
//        // subject - mandatory
//        if (subject() == null || subject().isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
//        }
//        for (Subject item : subject()) {
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
//        if ((version() == null || VcdmVersion.V11.equals(version()))
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
//
//    @Override
//    default boolean isCredential() {
//        return true;
//    }
//
//    @Override
//    default Credential3 asCredential() {
//        return this;
//    }
}
