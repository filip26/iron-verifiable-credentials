package com.apicatalog.vc;

import java.time.Instant;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.DataIntegrityError.Code;

import jakarta.json.JsonObject;

public class Credential {

    public static final String TYPE_VALUE = "https://www.w3.org/2018/credentials#VerifiableCredential";

    public static final String SUBJECT = "https://www.w3.org/2018/credentials#credentialSubject";
    public static final String ISSUER = "https://www.w3.org/2018/credentials#issuer";
    public static final String ISSUANCE_DATE = "https://www.w3.org/2018/credentials#issuanceDate";
    public static final String EXPIRATION_DATE = "https://www.w3.org/2018/credentials#expirationDate";

    private Instant expiration;

    public static boolean isCredential(JsonObject object) {
        if (object == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        return JsonLdUtils.isTypeOf(TYPE_VALUE, object);
    }

    public static Credential from(JsonObject json) throws DataIntegrityError {

        if (JsonLdUtils.hasTypeDeclaration(json)) {
            throw new DataIntegrityError(Code.MissingSubject);
        }

        if (!json.containsKey(SUBJECT)) {
            throw new DataIntegrityError(Code.MissingSubject);
        }

        if (!json.containsKey(ISSUER)) {
            throw new DataIntegrityError(Code.MissingIssuer);
        }

        if (!json.containsKey(ISSUANCE_DATE)) {
            throw new DataIntegrityError(Code.MissingIssuanceDater);
        }

        //TODO
        return null;
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
        //TODO
        return null;
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
