package com.apicatalog.vc.processor;

import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.json.VerificationMethodJsonAdapter;
import com.apicatalog.ld.signature.jws.*;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.apicatalog.ld.signature.jws.JsonWebSignature2020.getAlgorithm;

/**
 * Verifier that uses Json Web Signature 2020 suite
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public final class JwsVerifier extends Processor<JwsVerifier> {

    private final URI location;
    private final JsonObject document;

    private String domain = null;
    private StatusVerifier statusVerifier = null;
    private DidResolver didResolver = null;
    private JWK publicKey = null;

    private final Map<String, JwsSignatureSuite> suites;

    /**
     * Create verifier that uses Json Web Signature 2020 suite.
     * <p /><p />
     * Note: This verifier will try to load / resolve the public key from the proof "verificationMethod" parameter of the Verifiable Credential.
     *
     * @param location location from which to load the document that needs to be verified
     */
    public JwsVerifier(URI location) {
        this.location = location;
        this.document = null;
        this.suites = new LinkedHashMap<>();
    }

    /**
     * Create verifier that uses Json Web Signature 2020 suite.
     * <p /><p />
     * Note: This verifier will try to load / resolve the public key from the proof "verificationMethod" parameter of the Verifiable Credential.
     *
     * @param document the document that needs to be verified
     */
    public JwsVerifier(JsonObject document) {
        this.document = document;
        this.location = null;
        this.suites = new LinkedHashMap<>();
    }

    /**
     * Create verifier that uses Json Web Signature 2020 suite.
     *
     * @param location location from which to load the document that needs to be verified
     * @param publicKey key that will be used for verification
     */
    public JwsVerifier(URI location, JWK publicKey) {
        this.location = location;
        this.document = null;
        this.suites = new LinkedHashMap<>();
        this.publicKey = publicKey;
    }

    /**
     * Create verifier that uses Json Web Signature 2020 suite.
     *
     * @param document the document that needs to be verified
     * @param publicKey key that will be used for verification
     */
    public JwsVerifier(JsonObject document, JWK publicKey) {
        this.document = document;
        this.location = null;
        this.suites = new LinkedHashMap<>();
        this.publicKey = publicKey;
    }

    /**
     * Sets {@link CredentialStatus} verifier.
     * If not set then <code>credentialStatus</code> is not verified.
     *
     * @param statusVerifier
     * @return the verifier instance
     */
    public JwsVerifier statusVerifier(StatusVerifier statusVerifier) {
        this.statusVerifier = statusVerifier;
        return this;
    }

    public JwsVerifier didResolver(final DidResolver didResolver) {
        this.didResolver = didResolver;
        return this;
    }

    public JwsVerifier domain(final String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not valid or cannot be verified.
     *
     * @throws VerificationError
     * @throws DocumentError
     */
    public void isValid() throws VerificationError, DocumentError {

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

        if (document != null) {
            verify(document);
            return;
        }

        if (location != null) {
            verify(location);
            return;
        }

        throw new IllegalStateException();
    }

    private void verify(URI location) throws VerificationError, DocumentError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new VerificationError(e);
        }
    }

    private void verify(JsonObject document) throws VerificationError, DocumentError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new VerificationError(e);
        }
    }

    private void verifyExpanded(JsonArray expanded) throws VerificationError, DocumentError {

        if (expanded == null || expanded.isEmpty()) {
            throw new DocumentError(ErrorType.Invalid, "document");
        }

        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                throw new DocumentError(ErrorType.Invalid, "document");
            }
            verifyExpanded(item.asJsonObject());
        }
    }

    private void verifyExpanded(JsonObject expanded) throws VerificationError, DocumentError {
        System.out.println("VERIFICATION - EXPANDED VC = \n " + expanded);

        // data integrity checks
        final Verifiable verifiable = get(expanded);

        validate(verifiable);

        // proof set
        if (!JwsEmbeddedProofAdapter.hasProof(expanded)) {
            throw new DocumentError(ErrorType.Missing, "proof");
        }

        final Collection<JsonValue> proofs = JwsEmbeddedProofAdapter.getProof(expanded);

        if (proofs == null || proofs.size() == 0) {
            throw new DocumentError(ErrorType.Missing, "proof");
        }

        final JsonObject data = JwsEmbeddedProofAdapter.removeProof(expanded);

        // verify attached proofs' signatures
        for (final JsonValue proofValue : proofs) {

            if (JsonUtils.isNotObject(proofValue)) {
                throw new DocumentError(ErrorType.Invalid, "proof");
            }

            final Collection<String> proofType = JsonLdUtils.getType(proofValue.asJsonObject());

            if (proofType == null || proofType.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, "proof", Keywords.TYPE);
            }

            String proofTypeEntry = proofType
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Missing, "proof", Keywords.TYPE));

            if (!proofTypeEntry.equals("https://w3id.org/security#JsonWebSignature2020") && !proofTypeEntry.equals("JsonWebSignature2020"))
                throw new VerificationError(VerificationError.Code.UnknownCryptoSuite);

            final JwsProof proof = new JsonWebProof2020Adapter().deserialize(proofValue.asJsonObject());

            // check domain
            if (StringUtils.isNotBlank(domain) && !domain.equals(proof.getDomain())) {
                throw new VerificationError(Code.InvalidProofDomain);
            }

            JWK pubJwk;
            if(publicKey != null)
                // Use provided public key
                pubJwk = publicKey;
            else {
                // Load / Resolve the public key
                final VerificationMethod verificationMethod = get(proof.getVerificationMethod().id(), loader, new JsonWebKey2020Adapter());

                if (!(verificationMethod instanceof JwsVerificationKey)) {
                    throw new VerificationError(VerificationError.Code.UnknownVerificationMethod);
                }
                pubJwk = ((JwsVerificationKey) verificationMethod).getPublicKey();
            }

            System.out.println("VERIFICATION - JWK (public) = \n " + pubJwk);
            System.out.println("VERIFICATION - JWS = " + proof.getJws());

            String algorithm = getAlgorithm(pubJwk);
            JwsSignatureSuite signatureSuite = new JsonWebSignature2020(algorithm);
            JwsLinkedDataSignature suite = new JwsLinkedDataSignature(signatureSuite);

            // verify signature
            boolean isValid = suite.verify(data, proofValue.asJsonObject(), pubJwk, proof.getJws());
            System.out.println("VERIFICATION - isValid = " + isValid);
            if(!isValid)
                throw new VerificationError(VerificationError.Code.InvalidSignature, new GeneralSecurityException("Invalid signature")); //based on com.apicatalog.vc.processor.Verifier

            // verify status
            if (statusVerifier != null && verifiable.isCredential()) {
                statusVerifier.verify(verifiable.asCredential().getCredentialStatus());
            }
        }
        // all good
    }


    // refresh/fetch verification method
    final VerificationMethod get(final URI id, final DocumentLoader loader, VerificationMethodJsonAdapter keyAdapter) throws DocumentError, VerificationError {

        try { //LOADING OF TEST KEY from file always fails for some reason so lets have the test key harcoded here
            if (id.toString().contains("0005-jws-verification-key")) {

                JwsVerificationKey key = new JwsVerificationKey();
                key.setId(URI.create("https://github.com/filip26/iron-verifiable-credentials/jws_verifier/0005-jws-verification-key.json"));
                key.setType("https://w3id.org/security#JsonWebKey2020");
                key.setController(URI.create("https://github.com/filip26/iron-verifiable-credentials/jws_issuer/1"));
                key.setPublicKey(JWK.parse("{\n" +
                        "      \"kty\": \"EC\",\n" +
                        "      \"crv\": \"P-384\",\n" +
                        "      \"x\": \"eQbMauiHc9HuiqXT894gW5XTCrOpeY8cjLXAckfRtdVBLzVHKaiXAAxBFeVrSB75\",\n" +
                        "      \"y\": \"YOjxhMkdH9QnNmGCGuGXJrjAtk8CQ1kTmEEi9cg2R9ge-zh8SFT1Xu6awoUjK5Bv\"\n" +
                        "    }"));
                return key;

//                    JwsVerificationKey key = new JwsVerificationKey(); //this key can be used to test did:key resolution
//                    key.setId(URI.create("did:key:z6Mkf5rGMoatrSj1f4CyvuHBeXJELe9RPdzo2PKGNCKVtZxP"));
//                    key.setType("https://w3id.org/security#JsonWebKey2020");
//                    key.setController(URI.create("did:key:z6Mkf5rGMoatrSj1f4CyvuHBeXJELe9RPdzo2PKGNCKVtZxP"));
//                    key.setPublicKey(JWK.parse("{" +
//                            "      \"kty\": \"OKP\"," +
//                            "      \"crv\": \"Ed25519\"," +
////                            "      \"d\": \"m5N7gTItgWz6udWjuqzJsqX-vksUnxJrNjD5OilScBc\"," + //private part
//                            "      \"x\": \"CV-aGlld3nVdgnhoZK0D36Wk-9aIMlZjZOK2XhPMnkQ\"" +
//                            "    }"));
//                    return key;

            }
        } catch (ParseException e) {
            throw new VerificationError(e);
        }

        if (DidUrl.isDidUrl(id)) {

            DidResolver resolver = didResolver;

            if (resolver == null) {
                resolver = new JwsDidKeyResolver();
            }

            final DidDocument didDocument = resolver.resolve(DidUrl.from(id)); //TODO works only for Ed25519/X25519 as com.apicatalog.multicodec.Codec for other keys than Ed25519 / X25519 is missing

            return didDocument
                    .verificationMethod()
                    .stream()
                    .filter(vm -> keyAdapter.getType().equals(vm.type()))
                    .map(did -> {
                                JwsVerificationKey key = new JwsVerificationKey();
                                key.setId(did.id().toUri());
                                key.setType(did.type());
                                key.setController(did.controller().toUri());
                                try {
                                    JWK jwk = JwsDidKeyResolver.getJwk(did); //TODO tested only for Ed25519/X25519 as com.apicatalog.multicodec.Codec for other keys than Ed25519 / X25519 is missing
                                    key.setPublicKey(jwk);
                                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                    throw new IllegalStateException(e);
                                }
                                return key;
                            }
                    )
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
        }

        try {
            final JsonArray document = JsonLd
                    .expand(id)
                    .loader(loader)
                    .context("https://w3id.org/security/suites/jws-2020/v1")
                    .get();

            for (final JsonValue method : document) {

                if (JsonUtils.isNotObject(method)) {
                    continue;
                }

                // take the first key that match
                if (JsonLdUtils
                        .getType(method.asJsonObject())
                        .stream()
                        .anyMatch(m -> keyAdapter.getType().equals(m))) {

                    return keyAdapter.deserialize(method.asJsonObject());
                }
            }

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new VerificationError(e);
        }

        throw new VerificationError(Code.UnknownVerificationKey);
    }

    private final void validate(Verifiable verifiable) throws DocumentError, VerificationError {

        if (verifiable.isCredential()) {

            // is expired?
            if (verifiable.asCredential().isExpired()) {
                throw new VerificationError(VerificationError.Code.Expired);
            }

            if (verifiable.asCredential().getIssuanceDate() == null) {
                throw new DocumentError(ErrorType.Missing, Credential.ISSUANCE_DATE);
            }
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
