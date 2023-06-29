package com.apicatalog.vc.model;

import java.time.Instant;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable credentials (VC).
 *
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model/#credentials">Credentials</a>
 *      
 * @since 0.9.0
 */
public class Credential extends Verifiable {

    protected CredentialVersion version;
    
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
    
    /**
     * A date time when the credential has been issued. VC data model v1.1.
     * Deprecated in favor of {@link Credential#getValidFrom()}.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @since 0.8.1
     * 
     * @return a date time
     */
    public Instant getIssuanceDate() {
        return issuance;
    }
    
    public void setIssuanceDate(Instant issuance) {
        this.issuance = issuance;
    }

    /**
     * VC data model 1.1 only. Deprecated in favor of {@link Credential#getValidUntil()}. 
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
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
     * The date and time the credential ceases to be valid, 
     * which could be a date and time in the past. 
     * Note that this value represents the latest point in time at which 
     * the information associated with the credentialSubject property is valid.
     * VC data model version 2.0.
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
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuer">Issuerr</a>
     * @return {@link JsonObject} representing the issuer in an expanded form
     */
    public JsonValue getIssuer() {
        return issuer;
    }

    public void setIssuer(JsonValue issuer) {
        this.issuer = issuer;
    }
    
    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
     * 
     * @return
     */
    public JsonValue getStatus() {
        return status;
    }
    
    public void setStatus(JsonValue status) {
        this.status = status;
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
    
    public void setSubject(JsonValue subject) {
        this.subject = subject;
    }

    @Override
    public boolean isCredential() {
        return true;
    }

    @Override
    public Credential asCredential() {
        return this;
    }
    
    /**
     * Returns a verifiable credential model version
     * 
     * @return the credential model version
     */
    public CredentialVersion getVersion() {
        return version;
    }
}
