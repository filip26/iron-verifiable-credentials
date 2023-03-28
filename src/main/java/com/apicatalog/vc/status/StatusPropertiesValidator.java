package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;

import jakarta.json.JsonValue;

/**
 * Validates verifiable credential status required properties. 
 * 
 * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
 * 
 */
public class StatusPropertiesValidator implements StatusValidator {

    protected URI id;
    protected Collection<String> type;

    public static StatusPropertiesValidator from(final JsonValue document) throws DocumentError {

        final StatusPropertiesValidator status = new StatusPropertiesValidator();

        try {
            status.id = JsonLdReader.getId(document).orElseThrow(() -> new DocumentError(ErrorType.Missing, VcVocab.STATUS, LdTerm.ID));
            
            if (!JsonLdReader.hasType(document)) {
                throw new DocumentError(ErrorType.Missing, VcVocab.STATUS, LdTerm.TYPE);
            }
            
            status.type = JsonLdReader.getType(document.asJsonObject());

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

    @Override
    public void verify(JsonValue status) throws DocumentError, VerifyError {
        from(status);
    }
}
