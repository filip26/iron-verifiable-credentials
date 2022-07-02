package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

class Presentation implements Verifiable {

    public static final String BASE = "https://www.w3.org/2018/credentials#";

    public static final String TYPE = "VerifiablePresentation";

    public static final String HOLDER = "holder";

    public static final String VERIFIABLE_CREDENTIALS = "verifiableCredential";

    protected URI id;

    protected URI holder;

    protected Collection<Credential> credentials;

    protected Presentation() {}

    public static boolean isPresentation(JsonValue expanded) {
        if (expanded == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }

        return JsonUtils.isObject(expanded) && JsonLdUtils.isTypeOf(BASE + TYPE, expanded.asJsonObject());
    }

    public static Presentation from(JsonObject subject) throws DocumentError {

        if (subject == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }

        final Presentation presentation = new Presentation();

        // @type
        if (!JsonLdUtils.isTypeOf(BASE + TYPE, subject)) {

            if (!JsonLdUtils.hasType(subject)) {
                throw new DocumentError(ErrorType.Missing, Keywords.TYPE);
            }

            throw new DocumentError(ErrorType.Unknown, Keywords.TYPE);
        }

        // @id - optional
        if (JsonLdUtils.hasPredicate(subject, Keywords.ID)) {
            presentation.id = JsonLdUtils
                        .getId(subject)
                        .orElseThrow(() -> new DocumentError(ErrorType.Invalid, Keywords.ID));
        }
        // holder - optional
        if (JsonLdUtils.hasPredicate(subject, BASE + HOLDER)) {
            presentation.holder = JsonLdUtils.assertId(subject, BASE, HOLDER);
        }

        presentation.credentials = new ArrayList<>();

        // verifiableCredentials
        for (final JsonValue credential : JsonLdUtils.getObjects(subject, BASE + VERIFIABLE_CREDENTIALS)) {

            if (JsonUtils.isNotObject(credential)) {
                throw new DocumentError(ErrorType.Invalid, VERIFIABLE_CREDENTIALS);
            }

            presentation.credentials.add(Credential.from(credential.asJsonObject()));
        }

        return presentation;
    }

    @Override
    public boolean isPresentation() {
        return true;
    }

    @Override
    public Presentation asPresentation() {
        return this;
    }

    @Override
    public URI getId() {
        return id;
    }

    /**
     * see {@link https://www.w3.org/TR/vc-data-model/#dfn-holders}
     * @return
     */
    public URI getHolder() {
        return holder;
    }
}
