package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdType;
import com.apicatalog.vc.VcVocab;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Validates verifiable credential status required properties.
 * 
 * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
 */
public class StatusPropertiesValidator implements StatusValidator {

    protected URI id;
    protected Collection<String> type;

    public static StatusPropertiesValidator from(final JsonObject document) throws DocumentError {

        final StatusPropertiesValidator status = new StatusPropertiesValidator();

        final LdNode node = LdNode.of(document);

        status.id = node.id();
        if (status.id == null) {
            throw new DocumentError(ErrorType.Missing, VcVocab.STATUS, Term.ID);
        }
        
        final LdType type = node.type();

        if (!type.exists()) {
            throw new DocumentError(ErrorType.Missing, VcVocab.STATUS, Term.TYPE);
        }

        status.type = type.strings();

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
        if (JsonUtils.isNull(status)) {
            return;

        } else if (JsonUtils.isObject(status)) {
            from(status.asJsonObject());

        } else if (JsonUtils.isArray(status)) {
            for (final JsonValue item : status.asJsonArray()) {
                if (JsonUtils.isObject(item)) {
                    from(item.asJsonObject());
                } else {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.STATUS);
                }
            }
        }
        throw new DocumentError(ErrorType.Invalid, VcVocab.STATUS);
    }
}
