package com.apicatalog.vc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * Represents a verifiable credential (VC).
 *
 * @see <a href= "https://www.w3.org/TR/vc-data-model/#credentials">v1.1</a>
 * @see <a href= "https://w3c.github.io/vc-data-model/#credentials">v2.0</a>
 * 
 * @since 0.9.0
 */
public class Credential extends Verifiable  {

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
    
    protected Credential(ModelVersion version, JsonObject expanded) {
        super(version, expanded);
    }

    /**
     * A date time when the credential has been issued. VC data model v1.1.
     * Deprecated in favor of {@link Credential#validFrom()} by VC data model
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

    public Credential issuanceDate(Instant issuance) {
        this.issuance = issuance;
        return this;
    }

    /**
     * VC data model v1.1 only. Deprecated in favor of
     * {@link Credential#validUntil()} by VC data model v2.0.
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

    @Override
    public boolean isCredential() {
        return true;
    }

    @Override
    public Credential asCredential() {
        return this;
    }

    @Override
    public void validate() throws DocumentError {

        // @type - mandatory
        if (type == null || type.isEmpty()) {
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        // subject - mandatory
        if (subject == null || subject.isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
        }
        for (Subject item : subject) {
            item.validate();
        }

        // issuer 
        if (issuer == null) {
            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
        }
        issuer.validate();

        // status
        if (status != null) {
            for (final Status item : status) {
                item.validate();
            }
        }
        
        // v1
        if ((version == null || ModelVersion.V11.equals(version))
                && issuance == null) {
            // issuance date is a mandatory property
            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUANCE_DATE);
        }
        
        // model v1
        if ((issuance != null
                && expiration != null
                && issuance.isAfter(expiration))
                // model v2
                || (validFrom != null
                        && validUntil != null
                        && validFrom.isAfter(validUntil))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }
    
    
//    public void type(Collection<String> type) {
//        this.type = type;
//    }

    public void subject(Collection<Subject> subject) {
        this.subject = subject;
    }
    
    public void status(Collection<Status> status) {
        this.status = status;
    }

    public void issuer(IssuerDetails issuer) {
        this.issuer = issuer;
    }

    public JsonObject expand() {
        
        final LdNodeBuilder builder = new LdNodeBuilder(Json.createObjectBuilder(expanded));
        
        if (issuance != null) {
            builder.set(VcVocab.ISSUANCE_DATE).xsdDateTime(issuance);
        }
        
        if (expiration != null) {
            builder.set(VcVocab.EXPIRATION_DATE).xsdDateTime(expiration);
        }
        
        if (validFrom != null) {
            builder.set(VcVocab.VALID_FROM).xsdDateTime(validFrom);
        }
        
        if (validUntil != null) {
            builder.set(VcVocab.VALID_UNTIL).xsdDateTime(validUntil);
        }
        
        return builder.build();
    }

    @Override
    protected Predicate<String> termsFilter() {
        return super.termsFilter().and(term -> !TERMS.contains(term));
    }

    public static Credential of(ModelVersion version, JsonObject document) {
        // TODO Auto-generated method stub
        return null;
    }
}
