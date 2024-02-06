package com.apicatalog.vc.model.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNode;
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
        return LdNode.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document);
    }

    public static Presentation read(final ModelVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Presentation presentation = new Presentation(version);

        // @type
        if (!LdNode.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document)) {

            if (LdNode.hasType(document)) {
                throw new DocumentError(ErrorType.Unknown, Term.TYPE);
            }
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        final LdNode node = LdNode.of(document);

        // @id - optional
        presentation.setId(node.id());

        // holder - optional
        presentation.setHolder(node.node(VcVocab.HOLDER).id());

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
