package com.apicatalog.vc.model;

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
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
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

    protected Collection<Proof<?>> proofs;
    protected Collection<String> type;

    protected JsonStructure context;

    protected DocumentLoader loader;
    
    protected JsonObject expanded;

    protected Verifiable(ModelVersion version, JsonObject expanded, DocumentLoader loader) {
        this.version = version;
        this.loader = loader;
        this.expanded = expanded;
    }

    public URI id() {
        return id;
    }

    public Collection<String> type() {
        return type;
    }

    public Collection<Proof<? extends ProofValue>> proofs() {
        return proofs;
    }

    public void proofs(Collection<Proof<? extends ProofValue>> proofs) {

        this.proofs = proofs;
        
        // remove proofs
        if (proofs == null || proofs.isEmpty()) {
            this.expanded = EmbeddedProof.removeProof(expanded);
            return;
        }
        
        // set proofs
        if (proofs != null && proofs.size() > 0) {
            JsonArrayBuilder pa = Json.createArrayBuilder();
            for (Proof<?> p : proofs) {
                pa.add(p.expand());
            }
            this.expanded = EmbeddedProof.setProofs(expanded, proofs.stream().map(Proof::expand).toList());
        }
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
    public ModelVersion getVersion() {
        return version;
    }

    public static ModelVersion getVersion(final JsonObject object) throws DocumentError {

        final JsonValue contexts = object.get(Keywords.CONTEXT);

        for (final JsonValue context : JsonUtils.toCollection(contexts)) {
            if (JsonUtils.isScalar(context)
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
            }
        }
        return ModelVersion.V20;
    }

    public static Verifiable of(final ModelVersion version, final JsonObject expanded, final DocumentLoader loader) throws DocumentError {

        // is a credential?
        if (Credential.isCredential(expanded)) {
            // validate the credential object
            return Credential.of(version, expanded, loader);
        }

        // is a presentation?
        if (Presentation.isPresentation(expanded)) {
            // validate the presentation object
            return Presentation.of(version, expanded, loader);
        }

        // is not expanded JSON-LD object
        if (JsonUtils.isNull(expanded.get(Keywords.TYPE))) {
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    public abstract void validate() throws DocumentError;

    /**
     * Get document in an expanded form.
     *
     * @return a document in an expanded form
     *
     * @throws DocumentError
     */
    public abstract JsonObject expand();

    /**
     * Get document compacted using standard contexts.
     *
     * @return the document in compacted form
     *
     * @throws DocumentError
     */
    public JsonObject compact() throws DocumentError {
        return compact(expand(), context, loader);
    }

    /**
     * Get document in compacted form.
     *
     * @param customContext a context or an array of contexts used to compact the
     *                      document
     *
     * @return the document in compacted form
     *
     * @throws DocumentError
     */
    public JsonObject compact(final JsonStructure customContext) throws DocumentError {
        return compact(expand(), customContext, loader);
    }

    /**
     * Get document in compacted form.
     *
     * @param contextLocation a context used to compact the document
     *
     * @return the document in compacted form
     *
     * @throws DocumentError
     */
    public JsonObject compact(final URI contextLocation) throws DocumentError {
        try {
            return postCompact(JsonLd.compact(JsonDocument.of(expand()), contextLocation).loader(loader).get());

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    public Collection<Proof<? extends ProofValue>> removeProofs() {
        final Collection<Proof<? extends ProofValue>> tmp = proofs;
        this.proofs = null;
        return tmp;
    }

    public void context(JsonStructure context) {
        this.context = context;
    }

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
