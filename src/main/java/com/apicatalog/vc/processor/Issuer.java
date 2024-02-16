package com.apicatalog.vc.processor;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.SigningError.Code;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.model.EmbeddedProof;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.model.Verifiable;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public final class Issuer extends Processor<Issuer> {

    // mandatory properties
    private final URI location;
    private JsonObject document;

    private final KeyPair keyPair;

    protected final Proof draft;

    public Issuer(URI location, KeyPair keyPair, final Proof draft) {
        this.location = location;
        this.document = null;

        this.keyPair = keyPair;

        this.draft = draft;
    }

    public Issuer(JsonObject document, KeyPair keyPair, final Proof draft) {
        this.document = document;
        this.location = null;

        this.keyPair = keyPair;

        this.draft = draft;
    }

    /**
     * Get signed document in expanded form.
     *
     * @return the signed document in expanded form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    public JsonObject getExpanded() throws SigningError, DocumentError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }

        if (document == null && location != null) {
            document = fetchDocument();
        }

        if (document != null && keyPair != null && draft != null) {
            return sign();
        }

        throw new IllegalStateException();
    }

    /**
     * Get signed document in compacted form.
     *
     * @param contextLocation a context used to compact the document
     *
     * @return the signed document in compacted form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    public JsonObject getCompacted(final URI contextLocation) throws SigningError, DocumentError {

        final JsonObject signed = getExpanded();

        try {
            return postCompact(JsonLd.compact(JsonDocument.of(signed), contextLocation).loader(loader).get());

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    /**
     * Get signed document compacted using standard contexts.
     *
     * @return the signed document in compacted form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    public JsonObject getCompacted() throws SigningError, DocumentError {

        final JsonObject signed = getExpanded();

        return getCompacted(signed, getContext());
    }

    /**
     * Get signed document in compacted form.
     *
     * @param context a context or an array of contexts used to compact the document
     *
     * @return the signed document in compacted form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    JsonObject getCompacted(final JsonStructure context) throws SigningError, DocumentError {

        final JsonObject signed = getExpanded();

        return getCompacted(signed, context);
    }

    JsonObject getCompacted(final JsonObject signed, final JsonStructure context) throws SigningError, DocumentError {

        try {
            return postCompact(JsonLd
                    .compact(JsonDocument.of(signed), JsonDocument.of(context))
                    .loader(loader)
                    .get());

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    JsonObject postCompact(final JsonObject source) {

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

    final JsonObject sign() throws DocumentError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            if (expanded.size() == 1) {
                final JsonValue object = expanded.iterator().next();
                if (JsonUtils.isObject(object)) {
                    return sign(getVersion(document), object.asJsonObject(), keyPair, draft);
                }
            }
            throw new DocumentError(ErrorType.Invalid); // malformed input, not single object to sign has been found

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    final JsonObject sign(final ModelVersion version, final JsonObject expanded, final KeyPair keyPair,
            final Proof draft) throws SigningError, DocumentError {

        if (keyPair.privateKey() == null || keyPair.privateKey().length == 0) {
            throw new IllegalArgumentException("The private key is not provided, is null or an empty array.");
        }

        JsonObject object = expanded;

        final Verifiable verifiable = get(version, object);

        validate(verifiable);

        if (draft.getCryptoSuite() == null) {
            throw new SigningError(Code.UnsupportedCryptoSuite);
        }

        // add issuance date if missing
        if (verifiable.isCredential()
                && (verifiable.getVersion() == null
                        || ModelVersion.V11.equals(verifiable.getVersion()))
                && verifiable.asCredential().getIssuanceDate() == null) {

            final Instant issuanceDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);

            object = Json.createObjectBuilder(object)
                    .add(VcVocab.ISSUANCE_DATE.uri(), issuanceDate.toString())
                    .build();
        }

        final JsonObject data = EmbeddedProof.removeProof(object);

        JsonObject unsignedDraft = draft.toJsonLd();

        if (draft.getValue() != null) {
            unsignedDraft = draft.valueProcessor().removeProofValue(unsignedDraft);
        }

        final LinkedDataSignature ldSignature = new LinkedDataSignature(draft.getCryptoSuite());

        final byte[] signature = ldSignature.sign(data, keyPair.privateKey(), unsignedDraft);

        final JsonObject signedProof = draft.valueProcessor().setProofValue(unsignedDraft, signature);

        return EmbeddedProof.addProof(object, signedProof);
    }

    final void validate(Verifiable verifiable) throws SigningError, DocumentError {
        // is expired?
        if (verifiable.isCredential()) {
            if (verifiable.asCredential().isExpired()) {
                throw new SigningError(Code.Expired);
            }
            super.validateData(verifiable.asCredential());
        }
    }

    final JsonObject fetchDocument() throws DocumentError, SigningError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return json.asJsonObject();

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    final JsonArray getContext() {

        final Collection<String> urls = new HashSet<>();
        final JsonArrayBuilder contexts = Json.createArrayBuilder();

        // extract origin contexts
        if (document != null && document.containsKey(Keywords.CONTEXT)) {
            final JsonValue documentContext = document.get(Keywords.CONTEXT);
            if (JsonUtils.isString(documentContext)) {
                urls.add(((JsonString)documentContext).getString());
                contexts.add(documentContext);

            } else if (JsonUtils.isObject(documentContext)) {
                contexts.add(documentContext);

            } else if (JsonUtils.isArray(documentContext)) {
                for (final JsonValue context : documentContext.asJsonArray()) {
                    if (JsonUtils.isString(context)) {
                        urls.add(((JsonString)context).getString());
                    }
                    contexts.add(context);
                }
            }
        }

        final Collection<String> provided = draft.getContext(modelVersion);
        
        if (provided != null) {
            //use .stream().filter(Predicate.not(urls::contains))
            for (String url : provided) {
                if (!urls.contains(url)) {
                    urls.add(url);
                    contexts.add(Json.createValue(url));
                }
            }
        }
        
        return contexts.build();
    }
}
