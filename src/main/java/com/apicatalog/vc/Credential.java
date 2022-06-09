package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.lds.DataError;
import com.apicatalog.lds.DataError.ErrorType;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 *
 * see {@link https://www.w3.org/TR/vc-data-model/#credentials}
 */
public class Credential implements Verifiable {

    public static final String BASE = "https://www.w3.org/2018/credentials#";

    public static final String TYPE = "VerifiableCredential";

    // properties
    public static final String SUBJECT = "credentialSubject";
    public static final String ISSUER = "issuer";
    public static final String ISSUANCE_DATE = "issuanceDate";
    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String CREDENTIAL_STATUS = "credentialStatus";

    private URI id;     //TODO
    
    private URI issuer;

    private Instant issuance;

    private Instant expiration;

    private CredentialStatus status;

    protected Credential() {}

    public static boolean isCredential(JsonValue expanded) {
        if (expanded == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }
        return JsonUtils.isObject(expanded) && JsonLdUtils.isTypeOf(BASE + TYPE, expanded.asJsonObject());
    }

    public static Credential from(JsonObject expanded) throws DataError {

        if (expanded == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }

        final Credential credential = new Credential();

        // type
        if (!JsonLdUtils.isTypeOf(BASE + TYPE, expanded)) {

            if (!JsonLdUtils.hasType(expanded)) {
                throw new DataError(ErrorType.Missing, Keywords.TYPE);
            }

            throw new DataError(ErrorType.Unknown, Keywords.TYPE);
        }
        
        // id
        if (JsonLdUtils.hasProperty(expanded, Keywords.ID)) {            
            credential.id = JsonLdUtils.getId(expanded)
                    .orElseThrow(() -> new DataError(ErrorType.Invalid, Keywords.ID));
        }

        // subject
        if (!hasProperty(expanded, SUBJECT)) {
            throw new DataError(ErrorType.Missing, SUBJECT);
        }

        // issuer
        if (!hasProperty(expanded, ISSUER)) {
            throw new DataError(ErrorType.Missing, ISSUER);
        }

        credential.issuer = JsonLdUtils
                                .getId(getProperty(expanded, ISSUER))
                                .orElseThrow(() -> new DataError(ErrorType.Invalid, ISSUER, Keywords.ID));

        // issuance date
        if (!hasProperty(expanded, ISSUANCE_DATE)) {
            throw new DataError(ErrorType.Missing, ISSUANCE_DATE);
        }

        if (!JsonLdUtils.isXsdDateTime(getProperty(expanded, ISSUANCE_DATE))) {
            throw new DataError(ErrorType.Invalid, ISSUANCE_DATE, Keywords.TYPE);
        }

        credential.issuance = JsonLdUtils
                                    .findFirstXsdDateTime(getProperty(expanded, ISSUANCE_DATE))
                                    .orElseThrow(() -> new DataError(ErrorType.Invalid, ISSUANCE_DATE, Keywords.VALUE));

        // expiration date
        if (hasProperty(expanded, EXPIRATION_DATE)) {
            credential.expiration = JsonLdUtils.findFirstXsdDateTime(getProperty(expanded, EXPIRATION_DATE))
                    .orElseThrow(() -> {
                        if (!JsonLdUtils.isXsdDateTime(getProperty(expanded, EXPIRATION_DATE))) {
                            return new DataError(ErrorType.Invalid, EXPIRATION_DATE, Keywords.TYPE);
                        }
                        return new DataError(ErrorType.Invalid, EXPIRATION_DATE, Keywords.VALUE);
                    });
        }

        // status
        if (hasProperty(expanded, CREDENTIAL_STATUS)) {
            for (final JsonValue status : JsonUtils.toJsonArray(getProperty(expanded, CREDENTIAL_STATUS))) {
                credential.status = CredentialStatus.from(status);
                break;
            }
        }

        //TODO
        return credential;
    }

    protected static boolean hasProperty(JsonObject expanded, String property) {
        return JsonLdUtils.hasProperty(expanded, BASE, property);
    }

    protected static JsonValue getProperty(JsonObject expanded, String property) {
        return JsonLdUtils.getProperty(expanded, BASE, property).orElse(null);  //TODO throw something
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
     * see {@link https://www.w3.org/TR/vc-data-model/#credential-subject}
     */
    public void getCredentialSubject() {
        //TODO
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
}
