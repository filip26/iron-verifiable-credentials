package com.apicatalog.vc.issuer;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
import com.apicatalog.vc.model.EmbeddedProof;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.model.Verifiable;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public abstract class BaseIssuer extends Processor<BaseIssuer> implements Issuer {

    protected final KeyPair keyPair;
    protected final DocumentLoader loader;

    protected BaseIssuer(KeyPair keyPair, DocumentLoader loader) {
        this.keyPair = keyPair;
        this.loader = loader;
    }

    abstract JsonObject sign(JsonStructure context, JsonObject data, Proof draft) throws SigningError;

    @Override
    public SignedCredentials sign(URI location, DataIntegrityProof draft) throws SigningError, DocumentError {
        return sign(fetchDocument(location), draft);
    }

//    if (loader == null) {
//        // default loader
//        loader = SchemeRouter.defaultInstance();
//    }
//
//    if (bundledContexts) {
//        loader = new StaticContextLoader(loader);
//    }

    @Override
    public SignedCredentials sign(JsonObject document, final Proof draft) throws SigningError, DocumentError {
//
//        if (loader == null) {
//            // default loader
//            loader = SchemeRouter.defaultInstance();
//        }
//
//        if (bundledContexts) {
//            loader = new StaticContextLoader(loader);
//        }
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            if (expanded.size() == 1) {
                final JsonValue object = expanded.iterator().next();
                if (JsonUtils.isObject(object)) {
                    return sign(getVersion(document), document.get(Keywords.CONTEXT), object.asJsonObject(), draft, loader);
                }
            }
            throw new DocumentError(ErrorType.Invalid); // malformed input, not single object to sign has been found

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected SignedCredentials sign(final ModelVersion version, final JsonValue context, final JsonObject expanded,
            final Proof draft, final DocumentLoader loader) throws SigningError, DocumentError {

        if (keyPair.privateKey() == null || keyPair.privateKey().length == 0) {
            throw new IllegalArgumentException("The private key is not provided, is null or an empty array.");
        }

        JsonObject object = expanded;

        final Verifiable verifiable = get(version, object);

        validate(verifiable);

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

        final JsonObject signedProof = sign(
                JsonUtils.isScalar(context)
                        ? Json.createArrayBuilder().add(context).build()
                        : (JsonStructure) context,
                data,
                draft);

        return new SignedCredentials(EmbeddedProof.addProof(object, signedProof), draft);
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
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }
}
