package com.apicatalog.vc.processor;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;

import jakarta.json.JsonValue;

/**
 * see {@link https://www.w3.org/TR/vc-data-model/#status}
 *
 */
class CredentialStatus implements StatusVerifier.Status {

    private URI id;
    private String type;

    protected CredentialStatus() {

    }

    public static CredentialStatus from(final JsonValue object) throws DataError {

        final CredentialStatus status = new CredentialStatus();

        if (!JsonLdUtils.hasType(object)) {
            throw new DataError(ErrorType.Missing, "status", Keywords.TYPE);
        }

        status.id = JsonLdUtils.getId(object).orElseThrow(() -> new DataError(ErrorType.Missing, "status", Keywords.ID));

        return status;
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }
}
