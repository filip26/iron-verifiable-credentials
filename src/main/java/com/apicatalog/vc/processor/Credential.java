package com.apicatalog.vc.processor;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable credentials (VC).
 *
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model/#credentials">Credentials</a>
 */
class Credential implements Verifiable {

    public static final String BASE = "https://www.w3.org/2018/credentials#";

    public static final String TYPE = "VerifiableCredential";

    // known properties
    public static final String SUBJECT = "credentialSubject";
    public static final String ISSUER = "issuer";

    public static final String ISSUANCE_DATE = "issuanceDate";

    public static final String VALID_FROM = "validFrom";
    public static final String VALID_UNTIL = "validUntil";
    public static final String ISSUED = "issued";

    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String CREDENTIAL_STATUS = "credentialStatus";
    public static final String CREDENTIAL_SCHEMA = "credentialSchema";
    public static final String REFRESH_SERVICE = "refreshService";
    public static final String TERMS_OF_USE = "termsOfUse";
    public static final String EVIDENCE = "evidence";

    protected URI id;

    protected URI issuer;

    protected Instant issuance; // issuanceDate
    protected Instant issued;
    protected Instant expiration;   // expirationDate
    
    protected Instant validUntil;
    protected Instant validFrom;

    protected JsonValue status;
    protected JsonValue subject;

    protected Map<String, JsonValue> extensions;

    protected Credential() { /* protected */ }

    public static boolean isCredential(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }
        return JsonLdUtils.isTypeOf(BASE + TYPE, document);
    }

    public static Credential from(final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Credential credential = new Credential();

        // @type
        if (!JsonLdUtils.isTypeOf(BASE + TYPE, document)) {

            if (!JsonLdUtils.hasType(document)) {
                throw new DocumentError(ErrorType.Missing, Keywords.TYPE);
            }

            throw new DocumentError(ErrorType.Unknown, Keywords.TYPE);
        }
        
        // subject - mandatory
        if (!JsonLdUtils.hasPredicate(document, BASE + SUBJECT)) {
            throw new DocumentError(ErrorType.Missing, SUBJECT);
        }

        try {            
            // @id - optional
            credential.id = JsonLdUtils.getId(document).orElse(null);

            // subject @id - mandatory
            JsonLdUtils
                    .getId(document, BASE + SUBJECT)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, SUBJECT));

            // issuer - mandatory
            credential.issuer = JsonLdUtils
                                    .getId(document, BASE + ISSUER)
                                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, ISSUER));

            // issuance date - mandatory for verification
            credential.issuance = JsonLdUtils.getXsdDateTime(document, BASE + ISSUANCE_DATE).orElse(null);

            // validFrom - optional
            credential.validFrom = JsonLdUtils.getXsdDateTime(document, BASE + VALID_FROM).orElse(null);

            // validFrom - optional
            credential.validUntil = JsonLdUtils.getXsdDateTime(document, BASE + VALID_UNTIL).orElse(null);

            // issued - optional
            credential.issued = JsonLdUtils.getXsdDateTime(document, BASE + ISSUED).orElse(null);

            // expiration date - optional
            credential.expiration = JsonLdUtils.getXsdDateTime(document, BASE + EXPIRATION_DATE).orElse(null);

        } catch (InvalidJsonLdValue e) {
            if (Keywords.ID.equals(e.getProperty())) {
                throw new DocumentError(ErrorType.Invalid, Keywords.ID);
            }
            throw new DocumentError(ErrorType.Invalid, e.getProperty().substring(0, BASE.length()));
        }

        // subject
        
        
        // status
        JsonLdUtils
            .getObjects(document, BASE + CREDENTIAL_STATUS).stream()
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
     * see {@link https://www.w3.org/TR/vc-data-model/#status}
     * 
     * @return
     */
    public JsonValue getCredentialStatus() {
        return status;
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
