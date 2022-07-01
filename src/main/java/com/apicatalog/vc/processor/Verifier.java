package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.did.DidDocument;
import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.key.VerificationMethodAdapter;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.EmbeddedProofAdapter;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.apicatalog.vc.loader.StaticContextLoader;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class Verifier extends Processor<Verifier> {

    private final URI location;
    private final JsonObject document;

    private String domain = null;
    private StatusVerifier statusVerifier = null;
    private DidResolver didResolver = null;

    public Verifier(URI location) {
        this.location = location;
        this.document = null;
    }

    public Verifier(JsonObject document) {
        this.document = document;
        this.location = null;
    }

    /**
     * Sets {@link CredentialStatus} verifier.
     * If not set then <code>credentialStatus</code> is not verified.
     *
     * @param statusVerifier
     * @return
     */
    public Verifier statusVerifier(StatusVerifier statusVerifier) {
        this.statusVerifier = statusVerifier;
        return this;
    }

    public Verifier didResolver(final DidResolver didResolver) {
        this.didResolver = didResolver;
        return this;
    }

    public Verifier domain(final String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not valid or cannot be verified.
     * @throws VerificationError
     * @throws DataError
     */
    public void isValid() throws VerificationError, DataError {

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

    private void verify(URI location) throws VerificationError, DataError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

    private void verify(JsonObject document) throws VerificationError, DataError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader).base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

    private void verifyExpanded(JsonArray expanded) throws VerificationError, DataError {

        if (expanded == null || expanded.isEmpty()) {
            throw new DataError(ErrorType.Invalid, "document");
        }

        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                throw new VerificationError();
            }
            verifyExpanded(item.asJsonObject());
        }
    }

    private void verifyExpanded(JsonObject expanded) throws VerificationError, DataError {

        // data integrity checks
        final Verifiable verifiable = get(expanded, false);

        // is expired?
        if (verifiable.isCredential() && verifiable.asCredential().isExpired()) {
            throw new VerificationError(Code.Expired);
        }

        // proof set
        if (!EmbeddedProofAdapter.hasProof(expanded)) {
            throw new DataError(ErrorType.Missing, "proof");
        }

        final Collection<JsonValue> proofs = EmbeddedProofAdapter.getProof(expanded);

        if (proofs == null || proofs.size() == 0) {
            throw new DataError(ErrorType.Missing, "proof");
        }

        final JsonObject data = EmbeddedProofAdapter.removeProof(expanded);

        // verify attached proofs' signatures
        for (final JsonValue proofValue : proofs) {
    	
            if (JsonUtils.isNotObject(proofValue)) {
        	throw new DataError(ErrorType.Invalid, "proof");
            }

            final SignatureSuite signatureSuite = 
        	    			EmbeddedProofAdapter.getProofType(proofValue.asJsonObject())
                	    			.stream()
                	    			.filter(suites::containsKey)
                	    			.findFirst()
                	    			.map(suites::get)
          	    				.orElseThrow(() -> new VerificationError(Code.UnknownCryptoSuite));

            final Proof proof = signatureSuite.getProofAdapter().deserialize(proofValue.asJsonObject());

//TODO            // check proof type
//            if (!embeddedProof.isPresent()) {
//
//                // @type property
//                if (!JsonLdUtils.hasType(proofValue)) {
//                    throw new DataError(ErrorType.Missing, "proof", Keywords.TYPE);
//                }
//
//                throw new VerificationError(Code.UnknownCryptoSuite);
//            }
//
//            final Proof proof = embeddedProof.get();

            // check domain
            if (StringUtils.isNotBlank(domain) && !domain.equals(proof.getDomain())) {
                throw new VerificationError(Code.InvalidProofDomain);
            }

            final VerificationMethod verificationMethod = get(proof.getVerificationMethod().getId(), loader, signatureSuite.getProofAdapter().getKeyAdapter()); 

            if (!(verificationMethod instanceof VerificationKey)) {
        	throw new VerificationError(Code.UnknownVerificationMethod);
            }

            final LinkedDataSignature signature = new LinkedDataSignature(signatureSuite);

            // verify signature
            signature.verify(data, proofValue.asJsonObject(), (VerificationKey)verificationMethod, proof.getValue());

            // verify status
            if (statusVerifier != null && verifiable.isCredential()) {
                statusVerifier.verify(verifiable.asCredential().getCredentialStatus());
            }
        }
        // all good
        return;
    }
    

    // refresh/fetch verification method
    final VerificationMethod get(final URI id, final DocumentLoader loader, VerificationMethodAdapter keyAdapter) throws DataError, VerificationError {

        if (DidUrl.isDidUrl(id)) {

            DidResolver resolver = didResolver;

            if (resolver == null) {
                resolver = new DidKeyResolver();
            }

            final DidDocument didDocument = resolver.resolve(DidUrl.from(id));

            return didDocument
                        .getVerificationMethod()
                        .stream()
                        .filter(vm -> keyAdapter.getType().equals(vm.getType()))
                        .map(VerificationKey.class::cast)
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);
        }

        try {
            final JsonArray document = JsonLd
                                            .expand(id)
                                            .loader(loader)
                                            .context("https://w3id.org/security/suites/ed25519-2020/v1")
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
            throw new VerificationError(e);
        }

        throw new VerificationError(Code.UnknownVerificationKey);
    }
}
