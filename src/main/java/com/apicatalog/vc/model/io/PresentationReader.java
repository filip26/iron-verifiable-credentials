package com.apicatalog.vc.model.io;

import java.util.Collection;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.model.DataModelVersion;
import com.apicatalog.vc.model.Presentation;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class PresentationReader {

    protected PresentationReader() {
        // protected
    }

    public static boolean isPresentation(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return JsonLdReader.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document);
    }

    public static Presentation read(final DataModelVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Presentation presentation = new Presentation(version);

        // @type
        if (!JsonLdReader.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document)) {

            if (!JsonLdReader.hasType(document)) {
                throw new DocumentError(ErrorType.Missing, LdTerm.TYPE);
            }
            throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
        }

        try {

            // @id - optional
            presentation.setId(JsonLdReader.getId(document).orElse(null));

            // holder - optional
            presentation.setHolder(JsonLdReader.getId(document, VcVocab.HOLDER.uri()).orElse(null));

        } catch (InvalidJsonLdValue e) {
            if (Keywords.ID.equals(e.getProperty())) {
                throw new DocumentError(ErrorType.Invalid, LdTerm.ID);
            }
            throw new DocumentError(ErrorType.Invalid, VcVocab.HOLDER);
        }

        return presentation;
    }

    public static Collection<JsonValue> getCredentials(final JsonObject document) throws DocumentError {
        return JsonLdReader.getObjects(document, VcVocab.VERIFIABLE_CREDENTIALS.uri());
    }
}
