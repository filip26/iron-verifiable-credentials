package com.apicatalog.vc.processor;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.jws.*;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.apicatalog.ld.signature.jws.JsonWebSignature2020.getAlgorithm;

/**
 * Issuer that uses Json Web Signature 2020 suite
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public final class JwsIssuer extends Processor<JwsIssuer> {

    private final URI location;
    private final JsonObject document;

    private final JwsKeyPair keyPair;

    private final ProofOptions options;

    private final Map<String, JwsSignatureSuite> suites;

    /**
     * Create issuer that uses Json Web Signature 2020 suite
     *
     * @param location location from which to load the document that needs to be signed
     * @param keyPair public and private key
     * @param options proof options (proof without "jws" signature)
     */
    public JwsIssuer(URI location, JwsKeyPair keyPair, ProofOptions options) {
        this.location = location;
        this.document = null;

        this.keyPair = keyPair;

        this.options = options;

        this.suites = new LinkedHashMap<>();
    }

    /**
     * Create issuer using Json Web Signature 2020 suite
     *
     * @param document the document that needs to be signed
     * @param keyPair public and private key
     * @param options proof options (proof without "jws" signature)
     */
    public JwsIssuer(JsonObject document, JwsKeyPair keyPair, ProofOptions options) {
        this.document = document;
        this.location = null;

        this.keyPair = keyPair;

        this.options = options;

        this.suites = new LinkedHashMap<>();
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
     * @throws DocumentError
     */
    public JsonObject getCompacted(final URI contextLocation) throws SigningError, DocumentError {

        final JsonObject signed = getExpanded();

        try {
            return JsonLd.compact(JsonDocument.of(signed), contextLocation).loader(loader).get();

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new SigningError(e);
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
    public JsonObject getCompacted() throws SigningError, DocumentError  {

        final JsonArray context = Json
                .createArrayBuilder()
                .add("https://www.w3.org/2018/credentials/v1")
                .add("https://w3id.org/security/suites/jws-2020/v1")
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
     * @throws DocumentError
     */
    public JsonObject getCompacted(final JsonStructure context) throws SigningError, DocumentError {

        final JsonObject signed = getExpanded();

        try {
            return JsonLd.compact(JsonDocument.of(signed), JsonDocument.of(context)).loader(loader).get();

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(final URI documentLocation, final JwsKeyPair keyPair, final ProofOptions options) throws DocumentError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(documentLocation).loader(loader).base(base).get();

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(JsonObject document, JwsKeyPair keyPair, ProofOptions options) throws DocumentError, SigningError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).base(base).get();

            return sign(expanded, keyPair, options);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new SigningError(e);
        }
    }

    private final JsonObject sign(final JsonArray expanded, final JwsKeyPair keyPair, final ProofOptions options) throws SigningError, DocumentError {

        JsonObject object = JsonLdUtils
                .findFirstObject(expanded)
                .orElseThrow(() ->
                        // malformed input, not single object to sign has been found
                        new DocumentError(ErrorType.Invalid, "document")
                );

        final Verifiable verifiable = get(object);

        validate(verifiable);

//        final SignatureSuite signatureSuite = suites.get(options.type());
//
//        if (signatureSuite == null) {
//            throw new SigningError(SigningError.Code.UnknownCryptoSuite);
//        }

        JWK jwk = keyPair.getPrivateKey();
        String alg = getAlgorithm(jwk);
        JwsSignatureSuite signatureSuite = new JsonWebSignature2020(alg);

        JsonObject data = JwsEmbeddedProofAdapter.removeProof(object);

        // add issuance date if missing
        if (verifiable.isCredential() && verifiable.asCredential().getIssuanceDate() == null) {

            final Instant issuanceDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);

            data = Json.createObjectBuilder(data).add(Credential.BASE + Credential.ISSUANCE_DATE, issuanceDate.toString()).build();
            object = Json.createObjectBuilder(object).add(Credential.BASE + Credential.ISSUANCE_DATE, issuanceDate.toString()).build();
        }

//        final LinkedDataSignature suite = new LinkedDataSignature(signatureSuite);
//
//        JsonObject proof = signatureSuite.getProofAdapter().serialize(options.toUnsignedProof());
//
//        final byte[] signature = suite.sign(data, keyPair, proof);
//
//        proof = signatureSuite.getProofAdapter().setProofValue(proof, signature);
//
//        return JwsEmbeddedProofAdapter.addProof(object, proof);

        final JwsLinkedDataSignature suite = new JwsLinkedDataSignature(signatureSuite);

        JsonObject proof = signatureSuite.getProofAdapter().serialize(JsonWebSignature2020.toUnsignedJwsProof(options));

        String jws = suite.sign(data, jwk, proof);

        proof = signatureSuite.getProofAdapter().setProofValue(proof, jws);

        return JwsEmbeddedProofAdapter.addProof(object, proof);
    }

    private final void validate(Verifiable verifiable) throws SigningError {

        // is expired?
        if (verifiable.isCredential() && verifiable.asCredential().isExpired()) {
            throw new SigningError(SigningError.Code.Expired);
        }
    }

    @Override
    protected void addDefaultSuites() {
        //JsonWebSignature2020 supports all signature algorithms below
        suites.put("EdDSA", new JsonWebSignature2020("EdDSA"));
        suites.put("ES256K", new JsonWebSignature2020("ES256K"));
        suites.put("ES256", new JsonWebSignature2020("ES256"));
        suites.put("ES384", new JsonWebSignature2020("ES384"));
        suites.put("PS256", new JsonWebSignature2020("PS256"));
    }

}
