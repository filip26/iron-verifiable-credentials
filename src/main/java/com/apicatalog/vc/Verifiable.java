package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.uri.UriUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

/**
 * Represents a common ancestor for verifiable data.
 * 
 * @since 0.9.0
 */
public abstract class Verifiable {

    private static final Logger LOGGER = Logger.getLogger(Verifiable.class.getName());

    protected final ModelVersion version;

    protected URI id;

    protected Collection<Proof> proofs;
    protected Collection<String> type;

    protected JsonObject expanded;

    protected Verifiable(ModelVersion version, JsonObject expanded) {
        this.version = version;
        this.expanded = expanded;
    }

    public URI id() {
        return id;
    }

    public Collection<String> type() {
        return type;
    }

    public Collection<Proof> proofs() {
        return proofs;
    }

    public void proofs(Collection<Proof> proofs) {
        this.proofs = proofs;
    }

    public boolean isCredential() {
        return false;
    }

    public boolean isPresentation() {
        return false;
    }

    public Credential asCredential() {
        throw new ClassCastException();
    }

    public Presentation asPresentation() {
        throw new ClassCastException();
    }

    /**
     * Verifiable credentials data model version.
     * 
     * @return the data model version, never <code>null</code>
     */
    public ModelVersion version() {
        return version;
    }

    public static ModelVersion getVersion(final JsonObject object) throws DocumentError {

        final JsonValue contexts = object.get(Keywords.CONTEXT);

        for (final JsonValue context : JsonUtils.toCollection(contexts)) {
            if (JsonUtils.isString(context)
                    && UriUtils.isURI(((JsonString) context).getString())) {

                final String contextUri = ((JsonString) context).getString();

                if ("https://www.w3.org/2018/credentials/v1".equals(contextUri)) {
                    return ModelVersion.V11;
                }
                if ("https://www.w3.org/ns/credentials/v2".equals(contextUri)) {

                    if (JsonUtils.isNotArray(contexts)) {
                        LOGGER.log(Level.INFO,
                                "VC model requires @context declaration be an array, it is inconsistent with another requirement on compaction. Therefore this requirement is not enforced by Iron VC");
                    }

                    return ModelVersion.V20;
                }
            } else {
                throw new DocumentError(ErrorType.Invalid, Keywords.CONTEXT);
            }
        }
        return ModelVersion.V20;
    }

    public static Verifiable of(final ModelVersion version, final JsonObject expanded) throws DocumentError {

        // is a credential?
        if (Credential.isCredential(expanded)) {
            // validate the credential object
            return Credential.of(version, expanded);
        }

        // is a presentation?
        if (Presentation.isPresentation(expanded)) {
            // validate the presentation object
            return Presentation.of(version, expanded);
        }

        // is not expanded JSON-LD object
        if (JsonUtils.isNull(expanded.get(Keywords.TYPE))) {
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    public abstract void validate() throws DocumentError;

    static JsonObject compact(final JsonObject signed, final JsonStructure context, final DocumentLoader loader) throws DocumentError {

        try {
            return postCompact(JsonLd
                    .compact(JsonDocument.of(signed), JsonDocument.of(context))
                    .loader(loader)
                    .get());

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    static JsonObject postCompact(final JsonObject source) {

        JsonObject compacted = source;

        // TODO use options
        // make sure @context is the first key and an array
        if (!compacted.keySet().iterator().next().equals(Keywords.CONTEXT)) {
            final JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add(Keywords.CONTEXT, JsonUtils.toJsonArray(compacted.get(Keywords.CONTEXT)));

            compacted.entrySet().stream()
                    .filter(entry -> !Keywords.CONTEXT.equals(entry.getKey()))
                    .forEach(entry -> builder.add(entry.getKey(), entry.getValue()));

            compacted = builder.build();
        }
        return compacted;
    }
}
