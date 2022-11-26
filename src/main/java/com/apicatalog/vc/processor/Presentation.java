package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
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

    protected Collection<JsonObject> credentials;

    protected Presentation() {
    }

    public static boolean isPresentation(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return JsonLdReader.isTypeOf(BASE + TYPE, document);
    }

    public static Presentation from(final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Presentation presentation = new Presentation();

        // @type
        if (!JsonLdReader.isTypeOf(BASE + TYPE, document)) {

            if (!JsonLdReader.hasType(document)) {
                throw new DocumentError(ErrorType.Missing, Keywords.TYPE);
            }
            throw new DocumentError(ErrorType.Unknown, Keywords.TYPE);
        }

        try {

            // @id - optional
            presentation.id = JsonLdReader.getId(document).orElse(null);

            // holder - optional
            presentation.holder = JsonLdReader.getId(document, BASE + HOLDER).orElse(null);

        } catch (InvalidJsonLdValue e) {
            if (Keywords.ID.equals(e.getProperty())) {
                throw new DocumentError(ErrorType.Invalid, e.getProperty());
            }
            throw new DocumentError(ErrorType.Invalid, e.getProperty().substring(0, BASE.length()));
        }

        presentation.credentials = new ArrayList<>();

        // verifiableCredentials
        for (final JsonValue credential : JsonLdReader.getObjects(document, BASE + VERIFIABLE_CREDENTIALS)) {

            if (JsonUtils.isNotObject(credential)) {
                throw new DocumentError(ErrorType.Invalid, VERIFIABLE_CREDENTIALS);
            }

            presentation.credentials.add(credential.asJsonObject());
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

    public Collection<JsonObject> getCredentials() {
        return credentials;
    }
    
    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#dfn-holders">Holder</a>
     * @return {@link URI} identifying the holder
     */
    public URI getHolder() {
        return holder;
    }
}
