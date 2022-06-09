package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

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

        // @type
        if (!JsonLdUtils.isTypeOf(BASE + TYPE, expanded)) {

            if (!JsonLdUtils.hasType(expanded)) {
                throw new DataError(ErrorType.Missing, Keywords.TYPE);
            }

            throw new DataError(ErrorType.Unknown, Keywords.TYPE);
        }
        
        // @id - optional
        if (JsonLdUtils.hasPredicate(expanded, Keywords.ID)) {            
            credential.id = JsonLdUtils.getId(expanded)
                    .orElseThrow(() -> new DataError(ErrorType.Invalid, Keywords.ID));
        }

        // subject - mandatory
        if (!JsonLdUtils.hasPredicate(expanded, BASE + SUBJECT)) {
            throw new DataError(ErrorType.Missing, SUBJECT);
        }

        // issuer - mandatory
        credential.issuer = JsonLdUtils.assertId(expanded, BASE, ISSUER);
       
        // issuance date - mandatory
        credential.issuance = JsonLdUtils.assertXsdDateTime(expanded, BASE, ISSUANCE_DATE);
        
        // expiration date - optional            
        if (JsonLdUtils.hasPredicate(expanded, BASE + EXPIRATION_DATE)) {
            credential.expiration = JsonLdUtils.assertXsdDateTime(expanded, BASE, EXPIRATION_DATE);
        }

        // status
        final Optional<JsonValue> status = JsonLdUtils
                                                .getObject(expanded, BASE + CREDENTIAL_STATUS)
                                                .findFirst();

        if (status.isPresent()) {
            credential.status = CredentialStatus.from(status.get());
        }

        //TODO
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
