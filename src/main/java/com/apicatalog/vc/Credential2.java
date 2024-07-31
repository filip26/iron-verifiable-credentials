package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;

/**
 * Represents a verifiable credential (VC).
 *
 * @see <a href= "https://www.w3.org/TR/vc-data-model/#credentials">v1.1</a>
 * @see <a href= "https://w3c.github.io/vc-data-model/#credentials">v2.0</a>
 * 
 * @since 0.9.0
 */
public abstract class Credential2   {

    private static final Logger LOGGER = Logger.getLogger(Credential2.class.getName());
    
    protected static final Collection<String> TERMS;
    
    static {
        TERMS = new ArrayList<>();
        TERMS.add(VcVocab.ISSUANCE_DATE.uri());
        TERMS.add(VcVocab.EXPIRATION_DATE.uri());
        TERMS.add(VcVocab.VALID_UNTIL.uri());
        TERMS.add(VcVocab.VALID_FROM.uri());
        TERMS.add(VcVocab.SUBJECT.uri());
        TERMS.add(VcVocab.STATUS.uri());
        TERMS.add(VcVocab.ISSUER.uri());
        TERMS.add(VcVocab.TERMS_OF_USE.uri());
    }

    /** issuanceDate - v1.1 */
    protected Instant issuance;
    /** expirationDate - v1.1 */
    protected Instant expiration;

    /** model v2.0 - issanceDate replacement */
    protected Instant validUntil;
    /** model v2.0 - expirationDate replacement */
    protected Instant validFrom;

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<Subject> subject;
    
    protected Collection<Status> status;
    
    protected IssuerDetails issuer;    
    
    //TODO termsOfUse
    
    protected Credential2(ModelVersion version) {
//        super(version);
    }

    /**
     * A date time when the credential has been issued. VC data model v1.1.
     * Deprecated in favor of {@link Credential2#validFrom()} by VC data model
     * v2.0.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @since 0.8.1
     * 
     * @return a date time from which the credential claims are valid or
     *         <code>null</code>.
     */
    public Instant issuanceDate() {
        return issuance;
    }

    public Credential2 issuanceDate(Instant issuance) {
        this.issuance = issuance;
        return this;
    }

    /**
     * VC data model v1.1 only. Deprecated in favor of
     * {@link Credential2#validUntil()} by VC data model v2.0.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    public Instant expiration() {
        return expiration;
    }

    public void expiration(Instant expiration) {
        this.expiration = expiration;
    }

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
    public Instant validFrom() {
        return validFrom;
    }

    public void validFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * The date and time the credential ceases to be valid, which could be a date
     * and time in the past. Note that this value represents the latest point in
     * time at which the information associated with the credentialSubject property
     * is valid. VC data model version 2.0.
     * 
     * @return the date and time the credential ceases to be valid
     */
    public Instant validUntil() {
        return validUntil;
    }

    public void validUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * Checks if the credential is expired.
     *
     * @return <code>true</code> if the credential is expired
     */
    public boolean isExpired() {
        return (expiration != null && expiration.isBefore(Instant.now()))
                || (validUntil != null && validUntil.isBefore(Instant.now()));
    }

    /**
     * Checks if the credential is active, i.e. does not define validFrom property
     * or the property datetime is before now.
     * 
     * @since 0.90.0
     * 
     * @return <code>true</code> if the credential is active
     */
    public boolean isNotValidYet() {
        return (issuance != null && issuance.isAfter(Instant.now()))
                || (validFrom != null && validFrom.isAfter(Instant.now()));
    }

    /**
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuer">Issuerr</a>
     * @return {@link IssuerDetails} representing the issuer in an expanded form
     */
    public IssuerDetails issuer() {
        return issuer;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
     * 
     * @return
     */
    public Collection<Status> status() {
        return status;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#credential-subject">Credential
     *      Subject</a>
     * 
     * @return
     */
    public Collection<Subject> subject() {
        return subject;
    }

//    @Override
//    public boolean isCredential() {
//        return true;
//    }
//
//    @Override
//    public Credential2 asCredential() {
//        return this;
//    }
//
//    @Override
//    public void validate() throws DocumentError {
//
//        // @type - mandatory
//        if (type() == null || type().isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, Term.TYPE);
//        }
//
//        // subject - mandatory
//        if (subject == null || subject.isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
//        }
//        for (Subject item : subject) {
//            item.validate();
//        }
//
//        // issuer 
//        if (issuer == null) {
//            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
//        }
//        issuer.validate();
//
//        // status
//        if (status != null) {
//            for (final Status item : status) {
//                item.validate();
//            }
//        }
//        
////        // v1
////        if ((version == null || ModelVersion.V11.equals(version))
////                && issuance == null) {
////            // issuance date is a mandatory property
////            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUANCE_DATE);
////        }
//        
//        // model v1
//        if ((issuance != null
//                && expiration != null
//                && issuance.isAfter(expiration))
//                // model v2
//                || (validFrom != null
//                        && validUntil != null
//                        && validFrom.isAfter(validUntil))) {
//            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
//        }
//    }
//
//    @Override
//    public URI id() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Collection<String> type() {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
