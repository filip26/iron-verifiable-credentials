package com.apicatalog.vc.api;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.lds.DataError;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.SigningError;
import com.apicatalog.lds.SigningError.Code;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.proof.ProofOptions;
import com.apicatalog.vc.StaticContextLoader;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

public final class IssuerApi {

    private final URI location;
    private final JsonObject document;

    private final URI keyPairLocation;
    private final KeyPair keyPair;

    private final ProofOptions options;

    private DocumentLoader loader = null;

    protected IssuerApi(URI location, URI keyPairLocation, ProofOptions options) {
        this.location = location;
        this.document = null;

        this.keyPairLocation = keyPairLocation;
        this.keyPair = null;

        this.options = options;
    }

    protected IssuerApi(JsonObject document, KeyPair keyPair, ProofOptions options) {
        this.document = document;
        this.location = null;

        this.keyPair = keyPair;
        this.keyPairLocation = null;

        this.options = options;
    }

    public IssuerApi loader(DocumentLoader loader) {
        this.loader = loader;
        return this;
    }

    /**
     * Get signed document in expanded form.
     *
     * @return
     * @throws SigningError
     * @throws DataError
     */
    public JsonObject get() throws SigningError, DataError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        //TODO make it configurable
        loader = new StaticContextLoader(loader);

        if (document != null && keyPair != null)  {
            return sign(document, keyPair, options);
        }

        if (location != null && keyPairLocation != null)  {
            return sign(location, keyPairLocation, options);
        }

        throw new IllegalStateException();
    }

    /**
     * Get signed document in compacted form.
     *
     * @param context
     * @return
     * @throws SigningError
     * @throws DataError
     */
    public JsonObject getCompacted(URI context)  throws SigningError, DataError {

        final JsonObject signed = get();

        try {
            return JsonLd.compact(JsonDocument.of(signed), context).loader(loader).get();
        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(URI documentLocation, URI keyPairLocation, ProofOptions options) throws DataError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(documentLocation).loader(loader).get();

            // load key pair
            final JsonArray keys = JsonLd.expand(keyPairLocation).loader(loader).get();

            // TODO keyPair type must match options.type
            final Ed25519KeyPair2020 keyPair = Ed25519KeyPair2020.from(keys.getJsonObject(0)); // FIXME

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(JsonObject document, KeyPair keyPair, ProofOptions options) throws DataError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).get();

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private static final JsonObject sign(JsonArray expanded, KeyPair keyPair, ProofOptions options) throws SigningError, DataError {

        final LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());

        final JsonObject object = JsonLdUtils.findFirstObject(expanded).orElseThrow(() ->
                    new SigningError() // malformed input, not single object to sign has been found
                    //TODO ErrorCode
                );

        final Verifiable verifiable = Vc.get(object);

        // is expired?
        if (verifiable.isCredential() && verifiable.asCredential().isExpired()) {
            throw new SigningError(Code.Expired);
        }

        return signature.sign(object, options, keyPair);
    }
}
