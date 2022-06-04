package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.DataIntegrityError.ErrorType;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * 
 * see {@link https://www.w3.org/TR/vc-data-model/#credentials}
 */
public class Credential {

    public static final String SCHEMA = "https://www.w3.org/2018/credentials#";
    
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
    
    protected Credential() {}

    public static boolean isCredential(JsonValue value) {
        if (value == null) {
            throw new IllegalArgumentException("The 'value' parameter must not be null.");
        }
        return JsonUtils.isObject(value) && JsonLdUtils.isTypeOf(TYPE, value.asJsonObject());
    }

    public static Credential from(JsonObject object) throws DataIntegrityError {

        if (object == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        final Credential credential = new Credential(); 
        
        if (!JsonLdUtils.hasType(object)) {
            throw new DataIntegrityError(ErrorType.Missing, Keywords.TYPE);
        }
        
        if (!JsonLdUtils.isTypeOf(TYPE, object)) {
            throw new DataIntegrityError(ErrorType.Unknown, Keywords.TYPE);
        }

        if (!hasProperty(object, SUBJECT)) {
            throw new DataIntegrityError(ErrorType.Missing, SUBJECT);
        }

        if (!hasProperty(object, ISSUER)) {
            throw new DataIntegrityError(ErrorType.Missing, ISSUER);
        }
        
        credential.issuer = JsonLdUtils
                                .getId(getProperty(object, ISSUER))
                                .orElseThrow(() -> new DataIntegrityError(ErrorType.Invalid, ISSUER, Keywords.ID));

        if (!hasProperty(object, ISSUANCE_DATE)) {
            throw new DataIntegrityError(ErrorType.Missing, ISSUANCE_DATE);
        }
        
        if (!JsonLdUtils.isXsdDateTime(getProperty(object, ISSUANCE_DATE))) {
            throw new DataIntegrityError(ErrorType.Invalid, ISSUANCE_DATE, Keywords.TYPE);
        }

        credential.issuance = JsonLdUtils
                                    .getXsdDateTime(getProperty(object, ISSUANCE_DATE))
                                    .orElseThrow(() -> new DataIntegrityError(ErrorType.Invalid, ISSUANCE_DATE, Keywords.VALUE));
        
        if (hasProperty(object, EXPIRATION_DATE)) {
            credential.expiration = JsonLdUtils.getXsdDateTime(getProperty(object, EXPIRATION_DATE))
                    .orElseThrow(() -> new DataIntegrityError(ErrorType.Invalid, EXPIRATION_DATE, Keywords.VALUE));
        }
        
        //TODO
        return credential;
    }
    
    protected static boolean hasProperty(JsonObject object, String property) {
        return JsonLdUtils.hasProperty(object, SCHEMA, property);
    }
    
    protected static JsonValue getProperty(JsonObject object, String property) {
        return JsonLdUtils.getProperty(object, SCHEMA, property).orElse(null);  //TODO throw something
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

    public boolean isExpired() {
        return expiration != null && expiration.isAfter(Instant.now());
    }

}
