package com.apicatalog.vc.model.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.model.ModelVersion;
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

    public static Presentation read(final ModelVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Presentation presentation = new Presentation(version);

        // @type
        if (!JsonLdReader.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document)) {

            if (JsonLdReader.hasType(document)) {
                throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
            }
            throw new DocumentError(ErrorType.Missing, LdTerm.TYPE);
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

    public static Collection<JsonObject> getCredentials(final JsonObject document) throws DocumentError {

        JsonValue credentials = document.get(VcVocab.VERIFIABLE_CREDENTIALS.uri());

        if (JsonUtils.isNotArray(credentials)
                || credentials.asJsonArray().size() == 0) {
            return Collections.emptyList();
        }

        final Collection<JsonObject> result = new ArrayList<>(credentials.asJsonArray().size());

        for (final JsonValue cred : credentials.asJsonArray()) {
            if (JsonUtils.isNotObject(cred)
                    || JsonUtils.isNotArray(cred.asJsonObject().get(Keywords.GRAPH))
                    || cred.asJsonObject().getJsonArray(Keywords.GRAPH).size() != 1
                    || JsonUtils.isNotObject(cred.asJsonObject().getJsonArray(Keywords.GRAPH).get(0))) {
                throw new DocumentError(ErrorType.Invalid, VcVocab.CREDENTIALS_VOCAB);
            }

            result.add(cred.asJsonObject().getJsonArray(Keywords.GRAPH).getJsonObject(0));
        }

        return result;
    }
}
