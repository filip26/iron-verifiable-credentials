package com.apicatalog.vc.processor;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.SigningError.Code;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.proof.EmbeddedProof;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.VcTag;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.loader.StaticContextLoader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public final class Issuer extends Processor<Issuer> {

    // mandatory properties
    private final URI location;
    private final JsonObject document;

    private final KeyPair keyPair;

    protected final ProofOptions options;

    public Issuer(URI location, KeyPair keyPair, final ProofOptions options) {
        this.location = location;
        this.document = null;

        this.keyPair = keyPair;

        this.options = options;
    }

    public Issuer(JsonObject document, KeyPair keyPair, final ProofOptions options) {
        this.document = document;
        this.location = null;

        this.keyPair = keyPair;

        this.options = options;
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

        if (document != null && keyPair != null) {
            return sign(document, keyPair, options);
        }

        if (location != null && keyPair != null) {
            return sign(location, keyPair, options);
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
            return JsonLd.compact(JsonDocument.of(signed), contextLocation).loader(loader).get();

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

        final JsonArray context = Json.createArrayBuilder()
                .add("https://www.w3.org/2018/credentials/v1")
                .add("https://w3id.org/security/suites/ed25519-2020/v1").build();

        return getCompacted(context);
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

        try {
            return JsonLd.compact(JsonDocument.of(signed), JsonDocument.of(context)).loader(loader)
                    .get();

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    final JsonObject sign(final URI documentLocation, final KeyPair keyPair,
            final ProofOptions options) throws DocumentError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(documentLocation).loader(loader).base(base)
                    .get();

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    final JsonObject sign(final JsonObject document, final KeyPair keyPair,
            final ProofOptions options) throws DocumentError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    final JsonObject sign(final JsonArray expanded, final KeyPair keyPair,
            final ProofOptions options) throws SigningError, DocumentError {

        JsonObject object = JsonLdReader
                .findFirstObject(expanded)
                .orElseThrow(() -> new DocumentError(ErrorType.Invalid)); // malformed input, not single object to sign has been found

        final Verifiable verifiable = get(object);

        validate(verifiable);

        if (options.getSuite() == null) {
            throw new SigningError(Code.UnsupportedCryptoSuite);
        }

        // add issuance date if missing
        if (verifiable.isCredential() && verifiable.asCredential().getIssuanceDate() == null) {

            final Date issuanceDate = new Date(); //Instant.now().truncatedTo(ChronoUnit.SECONDS);

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

            
            object = Json.createObjectBuilder(object)
                    .add(VcVocab.ISSUANCE_DATE.uri(), formatter.format(issuanceDate))
                    .build();
        }

        final JsonObject data = EmbeddedProof.removeProof(object);

        final LinkedDataSignature ldSignature = new LinkedDataSignature(options.getSuite().getCryptoSuite());

        JsonObject proof = options.getSuite().getSchema().write(options.toUnsignedProof());

        final LdProperty<byte[]> proofValueProperty = options.getSuite().getSchema().tagged(VcTag.ProofValue.name());

        final byte[] signature = ldSignature.sign(data, keyPair, proof);

        final JsonValue proofValue = proofValueProperty.write(signature);

        proof = Json.createObjectBuilder(proof)
                .add(proofValueProperty.term().uri(),
                        Json.createArrayBuilder().add(proofValue))
                .build();

        return EmbeddedProof.addProof(object, proof);
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
}
