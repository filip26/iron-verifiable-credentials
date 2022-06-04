package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.DataIntegrityError.ErrorType;
import com.apicatalog.lds.SigningError.Code;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.lds.SigningError;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.proof.ProofOptions;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class SigningProcessor {

    private final URI location;
    private final JsonObject document;

    private final URI keyPairLocation;
    private final KeyPair keyPair;

    private final ProofOptions options;

    private DocumentLoader loader = null;

    protected SigningProcessor(URI location, URI keyPairLocation, ProofOptions options) {
        this.location = location;
        this.document = null;

        this.keyPairLocation = keyPairLocation;
        this.keyPair = null;

        this.options = options;
    }

    protected SigningProcessor(JsonObject document, KeyPair keyPair, ProofOptions options) {
        this.document = document;
        this.location = null;

        this.keyPair = keyPair;
        this.keyPairLocation = null;

        this.options = options;
    }

    public SigningProcessor loader(DocumentLoader loader) {
        this.loader = loader;
        return this;
    }

    /**
     * Get signed document in expanded form.
     *
     * @return
     * @throws SigningError
     * @throws DataIntegrityError
     */
    public JsonObject get() throws SigningError, DataIntegrityError {

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
     * @throws DataIntegrityError
     */
    public JsonObject getCompacted(URI context)  throws SigningError, DataIntegrityError {

        final JsonObject signed = get();

        try {
            return JsonLd.compact(JsonDocument.of(signed), context).get();
        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(URI documentLocation, URI keyPairLocation, ProofOptions options) throws DataIntegrityError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(documentLocation).loader(loader).get();

            // load key pair
            final Document keys = loader.loadDocument(keyPairLocation, new DocumentLoaderOptions());

            // TODO keyPair type must match options.type
            final Ed25519KeyPair2020 keyPair = Ed25519KeyPair2020.from(keys.getJsonContent().orElseThrow().asJsonObject()); // FIXME

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(JsonObject document, KeyPair keyPair, ProofOptions options) throws DataIntegrityError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).get();

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private static final JsonObject sign(JsonArray expanded, KeyPair keyPair, ProofOptions options) throws SigningError, DataIntegrityError {

        final LinkedDataSignature signature = new LinkedDataSignature(new Ed25519Signature2020());

        for (final JsonValue item : expanded) {

            // is a credential?
            if (Credential.isCredential(item)) {

                final JsonObject object = item.asJsonObject();
                
                // validate the credential object
                final Credential credential = Credential.from(object);

                // is expired?
                if (credential.isExpired()) {
                    throw new SigningError(Code.Expired);
                }

                final JsonObject signed = signature.sign(object, options, keyPair);

                // take only the first object
                return signed;

            }
            
            // is a presentation?
            if (Presentation.isPresentation(item)) {
                // validate the presentation object
                //TODO
            }
            
            // unknown or non-existent type

            // is not expanded JSON-LD object
            if (!JsonLdUtils.hasType(item)) {
                throw new DataIntegrityError(ErrorType.Missing, Keywords.TYPE);
            }

            throw new DataIntegrityError(ErrorType.Unknown, Keywords.TYPE);
        }

        throw new SigningError();     // malformed input, not single object to sign has been found
    }
}
