package com.apicatalog.vc.issuer;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.SigningError.Code;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.model.Verifiable;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public abstract class BaseIssuer<T extends ProofValue> implements Issuer<T> {

    protected final KeyPair keyPair;

    protected DocumentLoader loader;
    protected URI base;
    protected boolean bundledContexts;

    protected BaseIssuer(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    protected abstract void sign(JsonStructure context, JsonObject data, Proof<T> draft) throws SigningError;

    @Override
    public Verifiable sign(URI location, DataIntegrityProof<T> draft) throws SigningError, DocumentError {
        return sign(fetchDocument(location), draft);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends Issuer<T>> I base(URI base) {
        this.base = base;
        return (I) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends Issuer<T>> I loader(DocumentLoader loader) {
        this.loader = loader;
        return (I) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends Issuer<T>> I useBundledContexts(boolean enable) {
        this.bundledContexts = enable;
        return (I) this;
    }

    @Override
    public Verifiable sign(JsonObject document, final Proof<T> draft) throws SigningError, DocumentError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }

        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            if (expanded.size() == 1) {
                final JsonValue object = expanded.iterator().next();
                if (JsonUtils.isObject(object)) {

                    final ModelVersion version = Verifiable.getVersion(document);

                    return sign(version, getContext(version, document, draft), object.asJsonObject(), draft, loader);
                }
            }
            throw new DocumentError(ErrorType.Invalid); // malformed input, not single object to sign has been found

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable sign(final ModelVersion version, final JsonArray context, final JsonObject expanded,
            final Proof<T> draft, final DocumentLoader loader) throws SigningError, DocumentError {

        if (keyPair.privateKey() == null || keyPair.privateKey().length == 0) {
            throw new IllegalArgumentException("The private key is not provided, is null or an empty array.");
        }

        JsonObject object = expanded;

        final Verifiable verifiable = Verifiable.of(version, object, loader);

        // TODO do something with exceptions, unify
        if (verifiable.isCredential() && verifiable.asCredential().isExpired()) {
            throw new SigningError(Code.Expired);
        }

        verifiable.validate();

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

        // remove proofs
        Collection<Proof<?>> proofs = verifiable.removeProofs();

        // sign
        sign(context, verifiable.expand(), draft);

        if (proofs == null) {
            proofs = new ArrayList<>(1);
        }

        proofs.add(draft);
        verifiable.proofs(proofs);

        verifiable.context(context);

        return verifiable;

//        return new IssuedCredentials(EmbeddedProof.addProof(object, signedProof), context, loader);
    }

    final JsonArray getContext(ModelVersion version, JsonObject document, Proof<?> draft) {

        final Collection<String> urls = new HashSet<>();
        final JsonArrayBuilder contexts = Json.createArrayBuilder();

        // extract origin contexts
        if (document != null && document.containsKey(Keywords.CONTEXT)) {
            final JsonValue documentContext = document.get(Keywords.CONTEXT);
            if (JsonUtils.isString(documentContext)) {
                urls.add(((JsonString) documentContext).getString());
                contexts.add(documentContext);

            } else if (JsonUtils.isObject(documentContext)) {
                contexts.add(documentContext);

            } else if (JsonUtils.isArray(documentContext)) {
                for (final JsonValue context : documentContext.asJsonArray()) {
                    if (JsonUtils.isString(context)) {
                        urls.add(((JsonString) context).getString());
                    }
                    contexts.add(context);
                }
            }
        }

        final Collection<String> provided = draft.context(version);

        if (provided != null) {
            // use .stream().filter(Predicate.not(urls::contains))
            for (String url : provided) {
                if (!urls.contains(url)) {
                    urls.add(url);
                    contexts.add(Json.createValue(url));
                }
            }
        }

        return contexts.build();
    }

    final JsonObject fetchDocument(URI location) throws DocumentError, SigningError {
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
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }
}
