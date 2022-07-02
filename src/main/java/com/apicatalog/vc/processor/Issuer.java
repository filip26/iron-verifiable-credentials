package com.apicatalog.vc.processor;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.SigningError.Code;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.proof.EmbeddedProofAdapter;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.loader.StaticContextLoader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public final class Issuer extends Processor<Issuer> {

    private final URI location;
    private final JsonObject document;

    private final KeyPair keyPair;

    private final ProofOptions options;

    public Issuer(URI location, KeyPair keyPair, ProofOptions options) {
        this.location = location;
        this.document = null;

        this.keyPair = keyPair;

        this.options = options;
    }

    public Issuer(JsonObject document, KeyPair keyPair, ProofOptions options) {
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
     * @throws DataError
     */
    public JsonObject getExpanded() throws SigningError, DataError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }

        if (suites.isEmpty()) {
            addDefaultSuites();
        }

        if (document != null && keyPair != null)  {
            return sign(document, keyPair, options);
        }

        if (location != null && keyPair != null)  {
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
     * @throws DataError
     */
    public JsonObject getCompacted(final URI contextLocation) throws SigningError, DataError {

        final JsonObject signed = getExpanded();

        try {
            return JsonLd.compact(JsonDocument.of(signed), contextLocation).loader(loader).get();
        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    /**
     * Get signed document compacted using standard contexts.
     *
     * @return the signed document in compacted form
     * 
     * @throws SigningError 
     * @throws DataError 
     */
    public JsonObject getCompacted() throws SigningError, DataError  {

        final JsonArray context = Json
                                    .createArrayBuilder()
                                    .add("https://www.w3.org/2018/credentials/v1")
                                    .add("https://w3id.org/security/suites/ed25519-2020/v1")
                                    .build();

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
     * @throws DataError
     */
    public JsonObject getCompacted(final JsonStructure context) throws SigningError, DataError {

        final JsonObject signed = getExpanded();

        try {
            return JsonLd.compact(JsonDocument.of(signed), JsonDocument.of(context)).loader(loader).get();
        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(final URI documentLocation, final KeyPair keyPair, final ProofOptions options) throws DataError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(documentLocation).loader(loader).base(base).get();

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(JsonObject document, KeyPair keyPair, ProofOptions options) throws DataError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).base(base).get();

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(final JsonArray expanded, final KeyPair keyPair, final ProofOptions options) throws SigningError, DataError {

        JsonObject object = JsonLdUtils
        			.findFirstObject(expanded)
        			.orElseThrow(() ->
        				// malformed input, not single object to sign has been found
        				new DataError(ErrorType.Invalid, "document")
        				);

        final Verifiable verifiable = get(object, true);

        // is expired?
        if (verifiable.isCredential() && verifiable.asCredential().isExpired()) {
            throw new SigningError(Code.Expired);
        }

        final SignatureSuite signatureSuite = suites.get(options.type());

        if (signatureSuite == null) {
            throw new SigningError(Code.UnknownCryptoSuite);   
        }

        JsonObject data = EmbeddedProofAdapter.removeProof(object);

        // add issuance date if missing
        if (verifiable.isCredential() && verifiable.asCredential().getIssuanceDate() == null) {

            final Instant issuanceDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);

            data = Json.createObjectBuilder(data).add(Credential.BASE + Credential.ISSUANCE_DATE, issuanceDate.toString()).build();
            object = Json.createObjectBuilder(object).add(Credential.BASE + Credential.ISSUANCE_DATE, issuanceDate.toString()).build();
        }

        final LinkedDataSignature suite = new LinkedDataSignature(signatureSuite);

        JsonObject proof = signatureSuite.getProofAdapter().serialize(options.toUnsignedProof());

        final byte[] signature = suite.sign(data, keyPair, proof);

        proof = signatureSuite.getProofAdapter().setProofValue(proof, signature);
        
        return EmbeddedProofAdapter.addProof(object, proof);
    }
}
