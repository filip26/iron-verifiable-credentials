package com.apicatalog.vc.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.vc.VcVocab;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable presentation (VP).
 *
 * @see <a href= "https://www.w3.org/TR/vc-data-model/#presentations">v1.1</a>
 * @see <a href= "https://w3c.github.io/vc-data-model/#presentations">v2.0</a>
 * 
 * @since 0.9.0
 */
public class Presentation extends Verifiable {

    protected URI holder;

    protected Collection<Credential> credentials;

    protected Presentation(ModelVersion version, JsonObject expanded, DocumentLoader loader) {
        super(version, expanded, loader);
    }

    @Override
    public boolean isPresentation() {
        return true;
    }

    @Override
    public Presentation asPresentation() {
        return this;
    }

    public Collection<Credential> credentials() {
        return credentials;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#dfn-holders">Holder</a>
     * @return {@link URI} identifying the holder
     */
    public URI holder() {
        return holder;
    }

    public void credentials(Collection<Credential> credentials) {
        this.credentials = credentials;
    }

    @Override
    public void validate() throws DocumentError {
        /*TODO validate something, e.g. a presence of credentials */
    }
    
    public static boolean isPresentation(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document);
    }

    public static Presentation of(final ModelVersion version, final JsonObject document, final DocumentLoader loader) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Presentation presentation = new Presentation(version, document, loader);

        // @type
        if (!LdNode.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document)) {

            if (LdNode.hasType(document)) {
                throw new DocumentError(ErrorType.Unknown, Term.TYPE);
            }
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        final LdNode node = LdNode.of(document);

        // @id - optional
        presentation.id = node.id();

        // holder - optional
        presentation.holder = node.node(VcVocab.HOLDER).id();

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
    
    static JsonObject setCredentials(final JsonObject document, final Collection<Credential> credentials) throws DocumentError {

        JsonArrayBuilder builder = Json.createArrayBuilder();
        
        credentials.stream().map(c -> Json.createObjectBuilder()
                .add(Keywords.GRAPH,
                        Json.createArrayBuilder().add(c.expand())))
                .forEach(builder::add);
        
        return Json.createObjectBuilder(document)
                    .add(VcVocab.VERIFIABLE_CREDENTIALS.uri(), builder).build();
        
    }

    @Override
    public JsonObject expand() {
        if (expanded != null) {
            return expanded;
        }

        return null;
    }
}
