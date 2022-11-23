package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonValue;

/**
 * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
 *
 */
class CredentialStatus implements StatusVerifier.Status {

    private URI id;
    private Collection<String> type;

    protected CredentialStatus() { /* protected */ }

    public static CredentialStatus from(final JsonValue document) throws DocumentError {

        final CredentialStatus status = new CredentialStatus();

        if (!JsonLdUtils.hasType(document)) {
            throw new DocumentError(ErrorType.Missing, "StatusType");
        }

        try {
            status.type = JsonLdUtils.getType(document.asJsonObject());
            status.id = JsonLdUtils.getId(document).orElseThrow(() -> new DocumentError(ErrorType.Missing, "StatusId"));
            
        } catch (InvalidJsonLdValue e) {
            throw new DocumentError(ErrorType.Invalid, "StatusId");
        }

        return status;
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public Collection<String> getType() {
        return type;
    }
}
