package com.apicatalog.vc;

import java.time.Instant;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.DataIntegrityError.Code;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class Credential {

    public static final String TYPE_VALUE = "https://www.w3.org/2018/credentials#VerifiableCredential";

    public static final String SUBJECT = "https://www.w3.org/2018/credentials#credentialSubject";
    public static final String ISSUER = "https://www.w3.org/2018/credentials#issuer";
    public static final String ISSUANCE_DATE = "https://www.w3.org/2018/credentials#issuanceDate";
    public static final String EXPIRATION_DATE = "https://www.w3.org/2018/credentials#expirationDate";

    private Instant issuance;
    private Instant expiration;
    
    protected Credential() {
        
    }

    public static boolean isCredential(JsonObject object) {
        if (object == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        return JsonLdUtils.isTypeOf(TYPE_VALUE, object);
    }

    public static Credential from(JsonObject object) throws DataIntegrityError {

        if (object == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        final Credential credential = new Credential(); 
        
        if (!JsonLdUtils.hasTypeDeclaration(object)) {
            throw new DataIntegrityError();
        }
        
        if (!JsonLdUtils.isTypeOf(TYPE_VALUE, object)) {
            throw new DataIntegrityError();
        }

        if (!object.containsKey(SUBJECT)) {
            throw new DataIntegrityError(Code.MissingSubject);
        }

        if (!object.containsKey(ISSUER)) {
            throw new DataIntegrityError(Code.MissingIssuer);
        }

        credential.issuance = JsonLdUtils
                                    .getDateTime(object.get(ISSUANCE_DATE))
                                    .orElseThrow(() -> new DataIntegrityError(Code.MissingIssuanceDate));
        
        credential.expiration = JsonLdUtils.getDateTime(object.get(EXPIRATION_DATE)).orElse(null);
        
        //TODO
        return credential;
    }
        
    /**
     *
     * see {@link https://www.w3.org/TR/vc-data-model/#issuer}
     * @return
     */
    public String getIssuer() {
        //TODO
        return null;
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
