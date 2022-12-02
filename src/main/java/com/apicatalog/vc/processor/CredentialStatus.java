package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.vc.VcVocab;

import jakarta.json.JsonValue;

/**
 * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
 *
 */
class CredentialStatus {

    private URI id;
    private Collection<String> type;

    protected CredentialStatus() {
        /* protected */ }

    public static CredentialStatus from(final JsonValue document) throws DocumentError {

        final CredentialStatus status = new CredentialStatus();

        if (!JsonLdReader.hasType(document)) {
            throw new DocumentError(ErrorType.Missing, VcVocab.STATUS, LdTerm.TYPE);
        }

        try {
            status.type = JsonLdReader.getType(document.asJsonObject());
            status.id = JsonLdReader.getId(document).orElseThrow(() -> new DocumentError(ErrorType.Missing, VcVocab.STATUS, LdTerm.ID));

        } catch (InvalidJsonLdValue e) {
            throw new DocumentError(ErrorType.Invalid, VcVocab.STATUS, LdTerm.ID);
        }

        return status;
    }

    public URI getId() {
        return id;
    }

    public Collection<String> getType() {
        return type;
    }
}
