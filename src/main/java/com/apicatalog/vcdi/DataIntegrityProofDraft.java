package com.apicatalog.vcdi;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
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
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class DataIntegrityProofDraft extends ProofDraft {

    protected final JsonLdWriter writer;

    protected static final Collection<String> V1_CONTEXTS = Arrays.asList(
            "https://w3id.org/security/data-integrity/v2",
            "https://w3id.org/security/multikey/v1");

    protected static final Collection<String> V2_CONTEXTS = Arrays.asList(
            "https://www.w3.org/ns/credentials/v2");

    protected final DataIntegritySuite suite;
    protected final CryptoSuite crypto;

    protected final URI purpose;

    protected Instant created;
    protected Instant expires;

    protected String domain;
    protected String challenge;
    protected String nonce;

    ProofValue signature;

    public DataIntegrityProofDraft(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            VerificationMethod method,
            URI purpose) {
        super(method);
        this.suite = suite;
        this.crypto = crypto;
        this.purpose = purpose;
        this.writer = getWriter(suite);
    }

    protected static JsonLdWriter getWriter(DataIntegritySuite suite) {
        JsonLdWriter writer = new JsonLdWriter()
                .scan(DataIntegrityProof.class)
                .scan(VerificationMethod.class);
        
        if (suite.customTypes != null) {
            suite.customTypes.forEach(writer::scan);
        }
        
        return writer;
    }
//
//    public DataIntegrityProofDraft(
//            DataIntegritySuite suite,
//            CryptoSuite crypto,
//            URI method,
//            URI purpose) {
//        super(method);
//        this.suite = suite;
//        this.purpose = purpose;
//    }

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
    public VerifiableMaterial unsigned(DocumentLoader loader, URI base) throws DocumentError {

        try {
            DataIntegrityProof proof = FragmentComposer.create()
                    .set("id", id)
                    .set("cryptoSuite", crypto)
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

            JsonArray expanded = JsonLd.expand(JsonDocument.of(compacted)).loader(loader).base(base).get();

            Collection<String> context = Collections.emptyList();

            if (compacted.containsKey(JsonLdKeyword.CONTEXT)) {
                context = JsonLdContext.strings(compacted, context);
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
        JsonObject expanded = proof.expanded(); // FIXME

        return new GenericMaterial(
                proof.context(),
                compacted,
                expanded);
    }

    public URI id() {
        return id;
    }

//    @Override
//    public Collection<String> type() {
//        return List.of(VcdiVocab.TYPE.uri());
//    }

    public VerificationMethod method() {
        return method;
    }

    public URI previousProof() {
        return previousProof;
    }

    public URI purpose() {
        return purpose;
    }

    public Instant created() {
        return created;
    }

    public Instant expires() {
        return null;
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

//    protected LdNodeBuilder unsigned(LdNodeBuilder builder) {
//
//        super.unsigned(builder, suite.methodAdapter);
//        
//        builder.type(VcdiVocab.TYPE.uri());
//        builder.set(VcdiVocab.CRYPTO_SUITE).scalar("https://w3id.org/security#cryptosuiteString", suite.cryptosuiteName);
//        
//        builder.set(VcdiVocab.PURPOSE).id(purpose);
//        
//        builder.set(VcdiVocab.CREATED).xsdDateTime(created != null ? created : Instant.now());
//
//        if (domain != null) {
//            builder.set(VcdiVocab.DOMAIN).string(domain);
//        }
//        if (challenge != null) {
//            builder.set(VcdiVocab.CHALLENGE).string(challenge);
//        }
//        if (nonce != null) {
//            builder.set(VcdiVocab.NONCE).string(nonce);
//        }
//
//        return builder;
//    }

    @Override
    public void validate() throws DocumentError {
        DataIntegrityProof.assertNotNull(this::purpose, VcdiVocab.PURPOSE);
//        DataIntegrityProof.assertNotNull(this::cryptoSuite, VcdiVocab.CRYPTO_SUITE);
//
//        if (cryptoSuite().isUnknown()) {
//            throw new DocumentError(ErrorType.Unknown, VcdiVocab.CRYPTO_SUITE);
//        }

        if (method() != null && method().id() == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethodId");
        }

        if (created() != null && expires() != null && created().isAfter(expires())) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }
}
