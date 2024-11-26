package com.apicatalog.vcdi;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.FragmentComposer;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.jsonld.io.JsonLdWriter;
import com.apicatalog.multibase.MultibaseLiteral;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.model.ModelValidation;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class DataIntegrityProofDraft extends ProofDraft {

    protected final JsonLdWriter writer;

    protected final DataIntegritySuite suite;
    protected final CryptoSuite crypto;

    protected String domain;
    protected String challenge;
    protected String nonce;

    ProofValue signature;

    public DataIntegrityProofDraft(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            VerificationMethod method) {
        super(VcdiVocab.TYPE.uri(), method);
        this.suite = suite;
        this.crypto = crypto;
        this.writer = getWriter(suite);
    }

    protected static JsonLdWriter getWriter(DataIntegritySuite suite) {
        JsonLdWriter writer = new JsonLdWriter()
                .scan(DataIntegrityProof.class)
                .scan(VerificationMethod.class);

        if (suite.customTypes != null) {
            suite.customTypes.forEach(writer::scan);
        }

        // context reducer
        writer.context(VcdmVocab.CONTEXT_MODEL_V2,
                List.of(VcdiVocab.CONTEXT_MODEL_V2));

        return writer;
    }

    public DataIntegrityProofDraft challenge(String challenge) {
        this.challenge = challenge;
        return this;
    }

    public DataIntegrityProofDraft domain(String domain) {
        this.domain = domain;
        return this;
    }

    public DataIntegrityProofDraft nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    @Override
    public VerifiableMaterial unsigned(JsonLdContext documentContext, DocumentLoader loader, URI base) throws DocumentError {

        try {
            DataIntegrityProof proof = FragmentComposer.create()
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
                    .get(DataIntegrityProof.class);

            JsonObject compacted = writer.compacted(proof);

            JsonArray expanded = JsonLd.expand(JsonDocument.of(compacted))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base)
                    .get();

            JsonLdContext context = documentContext;

            if (compacted.containsKey(JsonLdKeyword.CONTEXT)) {
                JsonLdContext proofContext = JsonLdContext.of(compacted, context);

                if (proofContext != null && !proofContext.isEmpty()) {
                    if (context.isEmpty()) {
                        context = proofContext;
                    } else {
                        context = writer.contextReducer().reduce(context, proofContext);
                    }
                }

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

        JsonValue value = Json.createValue(suite.proofValueBase.encode(signature));

        JsonObject compacted = Json.createObjectBuilder(proof.compacted()).add(VcdiVocab.PROOF_VALUE.name(), value).build();
        JsonObject expanded = Json.createObjectBuilder(proof.expanded())
                .add(VcdiVocab.PROOF_VALUE.uri(),
                        Json.createArrayBuilder().add(
                                Json.createObjectBuilder()
                                        .add(JsonLdKeyword.VALUE, value)
                                        .add(JsonLdKeyword.TYPE, MultibaseLiteral.typeName())))
                .build();

        return new GenericMaterial(
                proof.context(),
                compacted,
                expanded);
    }

    @Override
    public void validate() throws DocumentError {
        ModelValidation.assertNotNull(this::purpose, VcdiVocab.PURPOSE);

        if (method() != null && method().id() == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethodId");
        }

        if (created() != null && expires() != null && created().isAfter(expires())) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }

    public DataIntegrityProofDraft created(Instant created) {
        this.created = created == null
                ? created
                : created.truncatedTo(ChronoUnit.SECONDS);
        return this;
    }

    public DataIntegrityProofDraft expires(Instant expires) {
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
