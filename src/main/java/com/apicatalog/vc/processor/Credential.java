package com.apicatalog.vc.processor;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.vc.VcVocab;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable credentials (VC).
 *
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model/#credentials">Credentials</a>
 */
class Credential implements Verifiable {

    protected URI id;

    protected URI issuer;

    protected Instant issuance; // issuanceDate
    protected Instant issued;
    protected Instant expiration; // expirationDate

    protected Instant validUntil;
    protected Instant validFrom;

    protected JsonValue status;
    protected JsonValue subject;

    protected Map<String, JsonValue> extensions;

    protected Credential() {
        /* protected */ }

    public static boolean isCredential(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }
        return JsonLdReader.isTypeOf(VcVocab.CREDENTIAL_TYPE.uri(), document);
    }

    public static Credential from(final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Credential credential = new Credential();

        // @type
        if (!JsonLdReader.isTypeOf(VcVocab.CREDENTIAL_TYPE.uri(), document)) {

            if (!JsonLdReader.hasType(document)) {
                throw new DocumentError(ErrorType.Missing, LdTerm.TYPE);
            }

            throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
        }

        // subject - mandatory
        if (!JsonLdReader.hasPredicate(document, VcVocab.SUBJECT.uri())) {
            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
        }

        try {
            // @id - optional
            credential.id = JsonLdReader.getId(document).orElse(null);

            // subject @id - mandatory 
//            JsonLdReader
//                    .getId(document, VcVocab.SUBJECT.uri())
//                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, VcVocab.SUBJECT));

            if (!JsonLdReader.hasPredicate(document, VcVocab.ISSUER.uri())) {
                throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
            }

            // issuer - mandatory
            credential.issuer = JsonLdReader
                    .getId(document, VcVocab.ISSUER.uri())
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, VcVocab.ISSUER));

            // issuance date - mandatory for verification
            credential.issuance = JsonLdReader.getXsdDateTime(document, VcVocab.ISSUANCE_DATE.uri()).orElse(null);

            // validFrom - optional
            credential.validFrom = JsonLdReader.getXsdDateTime(document, VcVocab.VALID_FROM.uri()).orElse(null);

            // validFrom - optional
            credential.validUntil = JsonLdReader.getXsdDateTime(document, VcVocab.VALID_UNTIL.uri()).orElse(null);

            // issued - optional
            credential.issued = JsonLdReader.getXsdDateTime(document, VcVocab.ISSUED.uri()).orElse(null);

            // expiration date - optional
            credential.expiration = JsonLdReader.getXsdDateTime(document, VcVocab.EXPIRATION_DATE.uri()).orElse(null);

        } catch (InvalidJsonLdValue e) {
            if (Keywords.ID.equals(e.getProperty())) {
                throw new DocumentError(ErrorType.Invalid, LdTerm.ID);
            }
            throw new DocumentError(ErrorType.Invalid, LdTerm.create(e.getProperty().substring(VcVocab.CREDENTIALS_VOCAB.length()), VcVocab.CREDENTIALS_VOCAB));
        }

        // status
        JsonLdReader
                .getObjects(document, VcVocab.STATUS.uri()).stream()
                .findFirst()
                .ifPresent(s -> credential.status = s);

        return credential;
    }

    @Override
    public URI getId() {
        return id;
    }

    /**
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuer">Issuerr</a>
     * @return {@link URI} identifying the issuer
     */
    public URI getIssuer() {
        return issuer;
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

    /**
     * Returns a map of predicates and objects that are not recognized by this
     * implementation.
     *
     * @return an immutable map of extensions
     */
    public Map<String, JsonValue> getExtensions() {
        return extensions;
    }
}
