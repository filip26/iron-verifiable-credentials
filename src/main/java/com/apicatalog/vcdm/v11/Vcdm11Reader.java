package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.JsonLdType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.proof.GenericProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model-1.1/">Verifiable Credentials Data
 * Model v1.1</a>
 */
public class Vcdm11Reader implements VerifiableReader {

    protected final JsonLdTreeReader reader = JsonLdTreeReader.create()

            .with(VcdmVocab.CREDENTIAL_TYPE.uri(),
                    Vcdm11Credential.class,
                    Vcdm11Credential::of)
            .with(VcdmVocab.PRESENTATION_TYPE.uri(),
                    Vcdm11Presentation.class,
                    Vcdm11Presentation::of)

            .with(XsdDateTime.typeAdapter())
            .build();

    protected final Collection<SignatureSuite> suites;

    public Vcdm11Reader(final SignatureSuite... suites) {
        this.suites = Arrays.asList(suites);
    }

    @Override
    public Verifiable read(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {

        try {
            Collection<String> context = JsonLdContext.strings(document);

            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base).get();

            return readExpanded(
                    VcdmVersion.V11,
                    context,
                    expanded,
                    loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    Verifiable readExpanded(
            final VcdmVersion version,
            final Collection<String> context,
            final JsonArray expanded,
            final DocumentLoader loader) throws DocumentError {

        if (expanded == null
                || expanded.size() != 1
                || ValueType.OBJECT != expanded.iterator().next().getValueType()) {
            throw new DocumentError(ErrorType.Invalid);
        }

        final JsonObject jsonVerifiable = expanded.iterator().next().asJsonObject();

        final JsonObject unsigned = EmbeddedProof.removeProofs(jsonVerifiable);

        try {
            // get a verifiable
            final LinkedTree tree = reader.read(context, Json.createArrayBuilder().add(unsigned).build());
            if (tree == null
                    || tree.size() != 1
                    || !tree.node().isFragment()
                    || !tree.node().asFragment().type().isAdaptableTo(Verifiable.class)) {
                throw new DocumentError(ErrorType.Invalid, "document");
            }

            final Verifiable verifiable = tree.object(Verifiable.class);

            // detach proofs
            final JsonArray jsonProofs = EmbeddedProof.getProofs(jsonVerifiable);

            final Collection<Proof> proofs = new ArrayList<>(jsonProofs.size());

            // read proofs
            for (final JsonValue jsonProofGraph : jsonProofs) {

                final JsonObject jsonProof = EmbeddedProof.getProof(jsonProofGraph);

                final Collection<String> proofTypes = JsonLdType.strings(jsonProof);
                
                if (proofTypes == null || proofTypes.size() != 1) {
                    throw new DocumentError(ErrorType.Invalid, "ProofType");
                }

                Proof proof = null;
                
                for (SignatureSuite suite : suites) {
                    if (suite.isSupported(verifiable, proofTypes.iterator().next(), jsonProof.asJsonObject())) {
                        proof = suite.getProof(verifiable, jsonProof.asJsonObject(), loader);
                        if (proof != null) {
                            break;
                        }
                    }
                }

                if (proof == null) {
                    var proofTree = reader.read(Json.createArrayBuilder().add(jsonProof).build());
                    proof = GenericProof.of(proofTree.fragment());
                }

                proofs.add(proof);
            }

            verifiable.proofs(proofs);

            return verifiable;

        } catch (DocumentError e) {
            throw e;

        } catch (TreeBuilderError | AdapterError e) {
            throw new DocumentError(e, ErrorType.Invalid, "document");
        }
    }
}
