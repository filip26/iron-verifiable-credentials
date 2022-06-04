package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;

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
public class Credential {

    public static final String BASE = "https://www.w3.org/2018/credentials#";
    
    public static final String TYPE = "VerifiableCredential";

    // properties
    public static final String SUBJECT = "credentialSubject";
    public static final String ISSUER = "issuer";
    public static final String ISSUANCE_DATE = "issuanceDate";
    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String CREDENTIAL_STATUS = "credentialStatus";

    private URI issuer;
    
    private Instant issuance;
    
    private Instant expiration;
    
    private CredentialStatus status;
    
    protected Credential() {}

    public static boolean isCredential(JsonValue value) {
        if (value == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }
        return JsonUtils.isObject(value) && JsonLdUtils.isTypeOf(BASE + TYPE, value.asJsonObject());
    }

    public static Credential from(JsonObject object) throws DataError {

        if (object == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        final Credential credential = new Credential(); 

        if (!JsonLdUtils.isTypeOf(BASE + TYPE, object)) {

            if (!JsonLdUtils.hasType(object)) {
                throw new DataError(ErrorType.Missing, Keywords.TYPE);
            }

            throw new DataError(ErrorType.Unknown, Keywords.TYPE);
        }

        if (!hasProperty(object, SUBJECT)) {
            throw new DataError(ErrorType.Missing, SUBJECT);
        }

        // issuer
        if (!hasProperty(object, ISSUER)) {
            throw new DataError(ErrorType.Missing, ISSUER);
        }
        
        credential.issuer = JsonLdUtils
                                .getId(getProperty(object, ISSUER))
                                .orElseThrow(() -> new DataError(ErrorType.Invalid, ISSUER, Keywords.ID));

        // issuance date
        if (!JsonLdUtils.isXsdDateTime(getProperty(object, ISSUANCE_DATE))) {
            
            if (!hasProperty(object, ISSUANCE_DATE)) {
                throw new DataError(ErrorType.Missing, ISSUANCE_DATE);
            }
            
            throw new DataError(ErrorType.Invalid, ISSUANCE_DATE, Keywords.TYPE);
        }

        credential.issuance = JsonLdUtils
                                    .getXsdDateTime(getProperty(object, ISSUANCE_DATE))
                                    .orElseThrow(() -> new DataError(ErrorType.Invalid, ISSUANCE_DATE, Keywords.VALUE));
        
        // expiration date
        if (hasProperty(object, EXPIRATION_DATE)) {
            credential.expiration = JsonLdUtils.getXsdDateTime(getProperty(object, EXPIRATION_DATE))
                    .orElseThrow(() -> {
                        if (!JsonLdUtils.isXsdDateTime(getProperty(object, EXPIRATION_DATE))) {
                            return new DataError(ErrorType.Invalid, EXPIRATION_DATE, Keywords.TYPE);
                        }
                        return new DataError(ErrorType.Invalid, EXPIRATION_DATE, Keywords.VALUE);
                    });
        }
        
        // status
        if (hasProperty(object, CREDENTIAL_STATUS)) {
            for (final JsonValue status : JsonUtils.toJsonArray(getProperty(object, CREDENTIAL_STATUS))) {
                credential.status = CredentialStatus.from(status);
                break;
            }
        }
        
        //TODO
        return credential;
    }
    
    protected static boolean hasProperty(JsonObject object, String property) {
        return JsonLdUtils.hasProperty(object, BASE, property);
    }
    
    protected static JsonValue getProperty(JsonObject object, String property) {
        return JsonLdUtils.getProperty(object, BASE, property).orElse(null);  //TODO throw something
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
}
