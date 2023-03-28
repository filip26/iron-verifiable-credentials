package com.apicatalog.vc.model;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.vc.VcVocab;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable credentials (VC).
 *
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model/#credentials">Credentials</a>
 */
public class Credential implements Verifiable {

    protected final JsonObject expanded;
    
    protected URI id;
    
    protected Collection<String> type;
    
    protected Collection<Proof> proof; 

    /** issuanceDate */
    protected Instant issuance; 
    protected Instant issued;
    /** expirationDate */
    protected Instant expiration;

    protected Instant validUntil;
    protected Instant validFrom;
    
    protected Credential(JsonObject expanded) {
        this.expanded = expanded;
    }

    public static boolean isCredential(final JsonValue expandedDocument) {
        if (expandedDocument == null) {
            throw new IllegalArgumentException("The 'expandedDocument' parameter must not be null.");
        }
        return JsonLdReader.isTypeOf(VcVocab.CREDENTIAL_TYPE.uri(), expandedDocument);
    }

    public static Credential from(final JsonObject expandedDocument) throws DocumentError {

        if (expandedDocument == null) {
            throw new IllegalArgumentException("The 'expandedDocument' parameter must not be null.");
        }

        final Credential credential = new Credential(expandedDocument);

        // @type
        credential.type = JsonLdReader.getType(expandedDocument);
        
        if (credential.type == null 
                || credential.type.isEmpty()
                || !credential.type.contains(VcVocab.CREDENTIAL_TYPE.uri())
                ) {

            if (!JsonLdReader.hasType(expandedDocument)) {
                throw new DocumentError(ErrorType.Missing, LdTerm.TYPE);
            }

            throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
        }

        // subject - mandatory
        if (!JsonLdReader.hasPredicate(expandedDocument, VcVocab.SUBJECT.uri())) {
            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
        }

        try {
            // @id - optional
            credential.id = JsonLdReader.getId(expandedDocument).orElse(null);

            if (!JsonLdReader.hasPredicate(expandedDocument, VcVocab.ISSUER.uri())) {
                throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
            }

            // issuer @id - mandatory
            JsonLdReader
                    .getId(expandedDocument, VcVocab.ISSUER.uri())
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, VcVocab.ISSUER));
            
            // issuance date - mandatory for verification
            credential.issuance = JsonLdReader.getXsdDateTime(expandedDocument, VcVocab.ISSUANCE_DATE.uri()).orElse(null);

            // validFrom - optional
            credential.validFrom = JsonLdReader.getXsdDateTime(expandedDocument, VcVocab.VALID_FROM.uri()).orElse(null);

            // validFrom - optional
            credential.validUntil = JsonLdReader.getXsdDateTime(expandedDocument, VcVocab.VALID_UNTIL.uri()).orElse(null);

            // issued - optional
            credential.issued = JsonLdReader.getXsdDateTime(expandedDocument, VcVocab.ISSUED.uri()).orElse(null);

            // expiration date - optional
            credential.expiration = JsonLdReader.getXsdDateTime(expandedDocument, VcVocab.EXPIRATION_DATE.uri()).orElse(null);

        } catch (InvalidJsonLdValue e) {
            if (Keywords.ID.equals(e.getProperty())) {
                throw new DocumentError(ErrorType.Invalid, LdTerm.ID);
            }
            throw new DocumentError(ErrorType.Invalid, LdTerm.create(e.getProperty().substring(VcVocab.CREDENTIALS_VOCAB.length()), VcVocab.CREDENTIALS_VOCAB));
        }

        return credential;
    }

    @Override
    public URI getId() {
        return id;
    }

    /**
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date</a>
     * @return the issuance date
     */
    public Instant getIssuanceDate() {
        return issuance;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>
     * @return the expiration date or <code>null</code> if not set
     */
    public Instant getExpiration() {
        return expiration;
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

    public Instant getValidUntil() {
        return validUntil;
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
        return expanded.get(VcVocab.ISSUER.uri());
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
     * 
     * @return
     */
    public JsonValue getStatus() {
        return expanded.get(VcVocab.STATUS.uri());
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#credential-subject">Credential
     *      Subject</a>
     * 
     * @return
     */
    public JsonValue getSubject() {
        return expanded.get(VcVocab.SUBJECT.uri());
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
     * Returns an expanded JSON-LD representation of the verifiable credentials.
     * 
     * @return the verifiable credentials in an expanded form
     */
    public JsonObject asExpandedJsonLd() {
        return expanded;
    }
}
