package com.apicatalog.vc.processor;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonValue;

/**
 * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
 *
 */
class CredentialStatus implements StatusVerifier.Status {

    private URI id;
    private String type;

    protected CredentialStatus() {

    }

    public static CredentialStatus from(final JsonValue object) throws DocumentError {

        final CredentialStatus status = new CredentialStatus();

        if (!JsonLdUtils.hasType(object)) {
            throw new DocumentError(ErrorType.Missing, "status", Keywords.TYPE);
        }

        status.id = JsonLdUtils.getId(object).orElseThrow(() -> new DocumentError(ErrorType.Missing, "status", Keywords.ID));

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
