package com.apicatalog.vc.processor;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable credentials (VC).
 *
 * @see {@link <a href="https://www.w3.org/TR/vc-data-model/#credentials">Credentials</a>}
 */
class Credential implements Verifiable {

    public static final String BASE = "https://www.w3.org/2018/credentials#";

    public static final String TYPE = "VerifiableCredential";

    // known properties
    public static final String SUBJECT = "credentialSubject";
    public static final String ISSUER = "issuer";
    public static final String ISSUANCE_DATE = "issuanceDate";
    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String CREDENTIAL_STATUS = "credentialStatus";
    public static final String CREDENTIAL_SCHEMA = "credentialSchema";
    public static final String REFRESH_SERVICE = "refreshService";
    public static final String TERMS_OF_USE = "termsOfUse";
    public static final String EVIDENCE = "evidence";

    protected URI id;

    protected URI issuer;

    protected Instant issuance;

    protected Instant expiration;

    protected CredentialStatus status;

    protected Collection<JsonValue> subjects;

    protected Map<String, JsonValue> extensions;

    protected Credential() {}

    public static boolean isCredential(JsonValue subject) {
        if (subject == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }
        return JsonUtils.isObject(subject) && JsonLdUtils.isTypeOf(BASE + TYPE, subject.asJsonObject());
    }

    public static Credential from(JsonObject subject) throws DocumentError {

        if (subject == null) {
            throw new IllegalArgumentException("The 'subject' parameter must not be null.");
        }

        final Credential credential = new Credential();

        // @type
        if (!JsonLdUtils.isTypeOf(BASE + TYPE, subject)) {

            if (!JsonLdUtils.hasType(subject)) {
                throw new DocumentError(ErrorType.Missing, Keywords.TYPE);
            }

            throw new DocumentError(ErrorType.Unknown, Keywords.TYPE);
        }

        // @id - optional
        if (JsonLdUtils.hasPredicate(subject, Keywords.ID)) {
            credential.id = JsonLdUtils.getId(subject)
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, Keywords.ID));
        }

        // subject - mandatory
        if (!JsonLdUtils.hasPredicate(subject, BASE + SUBJECT)) {
            throw new DocumentError(ErrorType.Missing, SUBJECT);
        }

        // subject @id
        JsonLdUtils.assertId(subject, BASE, SUBJECT);

        // issuer - mandatory
        credential.issuer = JsonLdUtils.assertId(subject, BASE, ISSUER);

        // issuance date - mandatory for verification
        if (JsonLdUtils.hasPredicate(subject, BASE + ISSUANCE_DATE)) {
            credential.issuance = JsonLdUtils.assertXsdDateTime(subject, BASE, ISSUANCE_DATE);
        }

        // expiration date - optional
        if (JsonLdUtils.hasPredicate(subject, BASE + EXPIRATION_DATE)) {
            credential.expiration = JsonLdUtils.assertXsdDateTime(subject, BASE, EXPIRATION_DATE);
        }

        // status
        final Optional<JsonValue> status = JsonLdUtils
                                                .getObjects(subject, BASE + CREDENTIAL_STATUS)
                                                .stream()
                                                .findFirst();

        if (status.isPresent()) {
            credential.status = CredentialStatus.from(status.get());
        }

        return credential;
    }

    @Override
    public URI getId() {
        return id;
    }

    /**
     *
     * see {@link https://www.w3.org/TR/vc-data-model/#issuer}
     * @return
     */
    public URI getIssuer() {
        return issuer;
    }

    /**
     *
     * see {@link https://www.w3.org/TR/vc-data-model/#issuance-date}
     * @return
     */
    public Instant getIssuanceDate() {
        return issuance;
    }

    /**
     * see {@link https://www.w3.org/TR/vc-data-model/#expiration}
     * @return
     */
    public Instant getExpiration() {
        return expiration;
    }

    /**
     * Checks if the credential is expired.
     *
     * @return <code>true</code> if the credential is expired
     */
    public boolean isExpired() {
        return expiration != null && expiration.isBefore(Instant.now());
    }

    /**
     * see {@link https://www.w3.org/TR/vc-data-model/#status}
     * @return
     */
    public CredentialStatus getCredentialStatus() {
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
     * Returns a map of predicates and objects that are not recognized by this implementation.
     *
     * @return an immutable map of extensions
     */
    public Map<String, JsonValue> getExtensions() {
        return extensions;
    }
}
