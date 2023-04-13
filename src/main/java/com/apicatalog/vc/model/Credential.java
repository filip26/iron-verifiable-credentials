package com.apicatalog.vc.model;

import java.time.Instant;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.vc.VcVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable credentials (VC).
 *
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model/#credentials">Credentials</a>
 */
public class Credential extends Verifiable {
        
    /** issuanceDate */
    protected Instant issuance; 
    protected Instant issued;
    /** expirationDate */
    protected Instant expiration;

    protected Instant validUntil;
    protected Instant validFrom;

    protected JsonValue subject;
    protected JsonValue issuer;
    protected JsonValue status;
    
    /**
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date</a>
     * @return the issuance date
     */
    public Instant getIssuanceDate() {
        return issuance;
    }
    
    public void setIssuanceDate(Instant issuance) {
        this.issuance = issuance;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>
     * @return the expiration date or <code>null</code> if not set
     */
    public Instant getExpiration() {
        return expiration;
    }
    
    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    /**
     * A date time when the credential has been issued.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @since 0.8.1
     * 
     * @return a date time
     */
    public Instant getIssued() {
        return issued;
    }
    
    public void setIssued(Instant issued) {
        this.issued = issued;
    }

    /**
     * A date time from the credential is valid.
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

    @Override
    public JsonObject toJsonLd() {

        final JsonObjectBuilder builder = Json.createObjectBuilder();
        
        if (id != null) {
            builder.add(Keywords.ID, id.toString());
        }
        
        if (type != null && !type.isEmpty()) {
            builder.add(Keywords.TYPE, Json.createArrayBuilder(type));
        }

        if (issuer != null) {
            builder.add(VcVocab.ISSUER.uri(), JsonUtils.toJsonArray(issuer));
        }
        
        if (issuance != null) {
            builder.add(VcVocab.ISSUANCE_DATE.uri(), Json.createArrayBuilder(
                    ).add(Json.createValue(issuance.toString())
                    ));
        }

        if (subject != null) {
            builder.add(VcVocab.SUBJECT.uri(), JsonUtils.toJsonArray(subject));
        }
        
        if (status != null) {
            builder.add(VcVocab.STATUS.uri(), JsonUtils.toJsonArray(status));
        }

        return builder.build();
    }
    
    @Override
    public String toString() {
        return toJsonLd().toString();
    }
}
