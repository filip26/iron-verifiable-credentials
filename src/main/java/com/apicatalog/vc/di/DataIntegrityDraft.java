package com.apicatalog.vc.di;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.FragmentComposer;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.json.JsonFragment;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.ModelAssertions;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

public class DataIntegrityDraft extends ProofDraft {

    protected final DataIntegritySuite suite;
    protected final CryptoSuite crypto;

    protected String domain;
    protected String challenge;
    protected String nonce;

    ProofValue signature;

    public DataIntegrityDraft(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            VerificationMethod method) {
        super(VcdiVocab.TYPE.uri(), method);
        this.suite = suite;
        this.crypto = crypto;
    }

    public DataIntegrityDraft challenge(String challenge) {
        this.challenge = challenge;
        return this;
    }

    public DataIntegrityDraft domain(String domain) {
        this.domain = domain;
        return this;
    }

    public DataIntegrityDraft nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    @Override
    public VerifiableMaterial unsigned(JsonLdContext documentContext, DocumentLoader loader, URI base) throws DocumentError {

        try {
            DataIntegrityProof unsginedProof = FragmentComposer.create()
                    .set("id", id)
                    .set("cryptosuite", crypto)
                    .set("purpose", purpose)
                    .set("created", created)
                    .set("expires", expires)
                    .set("method", method)
                    .set(VcdiVocab.PREVIOUS_PROOF.name(), previousProof)
                    .set(VcdiVocab.CHALLENGE.name(), challenge)
                    .set(VcdiVocab.NONCE.name(), nonce)
                    .set(VcdiVocab.DOMAIN.name(), domain)
                    .json(suite.writer::compact)
                    .get(DataIntegrityProof.class);

            JsonObject compacted = ((JsonFragment) unsginedProof).jsonObject();

            JsonArray expanded = JsonLd.expand(JsonDocument.of(compacted))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base)
                    .get();

            JsonLdContext context = JsonLdContext.of(compacted, documentContext);
            if (compacted.containsKey(JsonLdKeyword.CONTEXT)) {
                context = suite.writer.contextReducer().reduce(documentContext, context);
                compacted = Json.createObjectBuilder(compacted).remove(JsonLdKeyword.CONTEXT).build();
            }

            return new GenericMaterial(
                    context,
                    compacted,
                    expanded.iterator().next().asJsonObject());

        } catch (FragmentPropertyError e) {
            throw DocumentError.of(e);

        } catch (NodeAdapterError e) {
            throw new DocumentError(e, ErrorType.Invalid);

        } catch (JsonLdError e) {
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    @Override
    protected VerifiableMaterial sign(VerifiableMaterial proof, byte[] signature) throws DocumentError {
        return DataIntegrityProof.sign(
                proof,
                Json.createValue(suite.proofValueBase.encode(signature)));
    }

    @Override
    public void validate() throws DocumentError {
        ModelAssertions.assertNotNull(this::purpose, VcdiVocab.PURPOSE);

        if (method() != null && method().id() == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethodId");
        }

        if (created() != null && expires() != null && created().isAfter(expires())) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }

    public DataIntegrityDraft created(Instant created) {
        this.created = created == null
                ? created
                : created.truncatedTo(ChronoUnit.SECONDS);
        return this;
    }

    public DataIntegrityDraft expires(Instant expires) {
        this.expires = expires == null
                ? expires
                : expires.truncatedTo(ChronoUnit.SECONDS);
        return this;
    }

    public String domain() {
        return domain;
    }

    public String challenge() {
        return challenge;
    }

    public String nonce() {
        return nonce;
    }
}
