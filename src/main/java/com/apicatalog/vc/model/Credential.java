package com.apicatalog.vc.model;

import java.time.Instant;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.vc.VcVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable credential (VC).
 *
 * @see <a href= "https://www.w3.org/TR/vc-data-model/#credentials">v1.1</a>
 * @see <a href= "https://w3c.github.io/vc-data-model/#credentials">v2.0</a>
 * 
 * @since 0.9.0
 */
public class Credential extends Verifiable {

    /** issuanceDate - v1.1 */
    protected Instant issuance;
    /** expirationDate - v1.1 */
    protected Instant expiration;

    /** model v2.0 - issanceDate replacement */
    protected Instant validUntil;
    /** model v2.0 - expirationDate replacement */
    protected Instant validFrom;

    protected JsonValue subject;
    protected JsonValue issuer;
    protected JsonValue status;

    protected Credential(ModelVersion version, JsonObject expanded, DocumentLoader loader) {
        super(version, expanded, loader);
    }

    /**
     * A date time when the credential has been issued. VC data model v1.1.
     * Deprecated in favor of {@link Credential#getValidFrom()} by VC data model
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
    public Instant getIssuanceDate() {
        return issuance;
    }

    public void setIssuanceDate(Instant issuance) {
        this.issuance = issuance;
    }

    /**
     * VC data model v1.1 only. Deprecated in favor of
     * {@link Credential#getValidUntil()} by VC data model v2.0.
     * 
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
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
    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
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
    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
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
     * @return {@link JsonObject} representing the issuer in an expanded form
     */
    public JsonValue getIssuer() {
        return issuer;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
     * 
     * @return
     */
    public JsonValue getStatus() {
        return status;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#credential-subject">Credential
     *      Subject</a>
     * 
     * @return
     */
    public JsonValue getSubject() {
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
    
    public static boolean isCredential(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcVocab.CREDENTIAL_TYPE.uri(), document);
    }

    public static Credential of(final ModelVersion version, final JsonObject document, final DocumentLoader loader) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Credential credential = new Credential(version, document, loader);

        final LdNode node = LdNode.of(document);
        
        // @type
        credential.type = node.type().strings();
        if (!credential.type().contains(VcVocab.CREDENTIAL_TYPE.uri())) {
            if (credential.type().isEmpty()) {
                throw new DocumentError(ErrorType.Missing, Term.TYPE);
            }
            throw new DocumentError(ErrorType.Unknown, Term.TYPE);
        }

        // subject - mandatory
        if (!node.node(VcVocab.SUBJECT).exists()) {
            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
        }

        credential.subject = document.get(VcVocab.SUBJECT.uri());

        // @id - optional
        credential.id = node.id();

        final LdNode issuer = node.node(VcVocab.ISSUER);
        
        if (!issuer.exists()) {
            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
        }
        
        // issuer @id - mandatory
        if (issuer.id() == null) {
            throw new DocumentError(ErrorType.Invalid, VcVocab.ISSUER);
        }
        
        credential.issuer = (document.get(VcVocab.ISSUER.uri()));

        credential.status = (document.get(VcVocab.STATUS.uri()));

        // issuance date - mandatory for verification
        credential.setIssuanceDate(node.scalar(VcVocab.ISSUANCE_DATE).xsdDateTime());

        // expiration date - optional
        credential.setExpiration(node.scalar(VcVocab.EXPIRATION_DATE).xsdDateTime());

        // validFrom - optional
        credential.setValidFrom(node.scalar(VcVocab.VALID_FROM).xsdDateTime());

        // validUntil - optional
        credential.setValidUntil(node.scalar(VcVocab.VALID_UNTIL).xsdDateTime());
        
        return credential;
    }

    @Override
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
}
