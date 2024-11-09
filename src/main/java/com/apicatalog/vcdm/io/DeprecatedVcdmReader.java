package com.apicatalog.vcdm.io;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.proxy.PropertyValueConsumer;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * An abstract W3C VCDM JSON-LD based reader.
 */
public abstract class DeprecatedVcdmReader  {

    protected final JsonLdTreeReader reader;

//    protected final Collection<SignatureSuite> suites;

    protected final VcdmVersion version;

    protected DeprecatedVcdmReader(VcdmVersion version, JsonLdTreeReader reader, final SignatureSuite... suites) {
        this.version = version;
//        this.suites = Arrays.asList(suites);
        this.reader = reader;
    }

//    @Override
//    public Verifiable read(
//            final Collection<String> context,
//            final JsonObject document,
//            final DocumentLoader loader,
//            final URI base) throws DocumentError {
//
//        final Collection<String> types = compactedType(document);
//
//        if (types == null || types.isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
//        }
//
//        if (isCredential(types)) {
//            return readCredential(context, document, loader, base);
//
//        } else if (isPresentation(types)) {
//            return readPresentation(context, document, loader, base);
//        }
//
//        throw new DocumentError(ErrorType.Unknown, JsonLdKeyword.TYPE);
//    }

    protected Credential readCredential(
            final Collection<String> context,
            final JsonObject document,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        try {
            // expand the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .context(Json.createArrayBuilder(context).build())
                    .loader(loader)
                    .base(base).get();

            if (expanded == null
                    || expanded.size() != 1
                    || ValueType.OBJECT != expanded.iterator().next().getValueType()) {
                throw new DocumentError(ErrorType.Invalid, "Credential");
            }

            return (Credential) read(
                    context,
                    document,
                    expanded.iterator().next().asJsonObject(),
                    loader,
                    base);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Presentation readPresentation(
            final Collection<String> context,
            final JsonObject document,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        // remove credentials
        final JsonObject jsonPresentation = Json.createObjectBuilder(document)
                .remove(VcdmVocab.VERIFIABLE_CREDENTIALS.name())
                .build();

        try {
            // expand the presentation
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(jsonPresentation))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base)
                    .get();

//            final Presentation presentation = (Presentation) read(
//                    context,
//                    jsonPresentation,
//                    expanded,
//                    loader,
//                    base);

//            if (presentation instanceof DeprecatedVcdmPresentation vcdmPresentation) {
//                vcdmPresentation.credentials(
//                        getCredentials(context, document.get(VcdmVocab.VERIFIABLE_CREDENTIALS.name()), loader, base));
//            }
//
//            return presentation;

            return null;
        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable read(
            final Collection<String> context,
            final JsonObject compacted,
            final JsonObject expanded,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        try {
//            // get a verifiable
//            final LinkedTree tree = reader.read(
//                    context,
//                    expanded,
//                    // drop main proofs
//                    ((node, indexOrder, indexTerm, depth) -> (VcdmVocab.PROOF.uri().equals(indexTerm)
//                            || VcdmVocab.VERIFIABLE_CREDENTIALS.uri().equals(indexTerm))
//                                    ? TraversalPolicy.Drop
//                                    : TraversalPolicy.Accept));
//
//            if (tree == null) {
//                throw new DocumentError(ErrorType.Invalid, "document");
//            }
//            if ((tree.size() != 1
//                    || !tree.node().isFragment()
//                    || !tree.node().asFragment().type().isAdaptableTo(Verifiable.class))) {
//                throw new DocumentError(ErrorType.Invalid, "document");
//            }
//
//            final Verifiable verifiable = tree.materialize(Verifiable.class);

            final JsonObject expandedUnsigned = EmbeddedProof.removeProofs(expanded);

            final Verifiable verifiable = reader.read(Verifiable.class, Json.createArrayBuilder().add(expandedUnsigned).build());

            final Collection<JsonObject> compactedProofs = EmbeddedProof.compactedProofs(compacted);

            if (compactedProofs != null
                    && !compactedProofs.isEmpty()
                    && verifiable instanceof PropertyValueConsumer proofConsumer) {

                final JsonObject compactedUnsigned = Json.createObjectBuilder(compacted).remove(VcdmVocab.PROOF.name()).build();

                final VerifiableMaterial data = new GenericMaterial(
                        context,
                        compactedUnsigned,
                        expandedUnsigned);

//                final JsonArray expandedProofs = EmbeddedProof.expandedProofs(expanded);
//
//                proofConsumer.acceptFragmentPropertyValue("proofs", getProofs(data, compactedProofs, expandedProofs, loader));
            }

            return verifiable;

        } catch (DocumentError e) {
            throw e;
            
        } catch (FragmentPropertyError e) {
            throw DocumentError.of(e);

        } catch (TreeBuilderError | NodeAdapterError e) {
            throw new DocumentError(e, ErrorType.Invalid, "Document");
        }
    }

    protected Collection<Proof> getProofs(
            VerifiableMaterial verifiable,
            Collection<JsonObject> compacted,
            JsonArray expanded,
            DocumentLoader loader) throws DocumentError {

        if (compacted.size() != expanded.size()) {
            throw new DocumentError(ErrorType.Invalid, "ProofIntegrity");
        }

        try {
            final Collection<Proof> proofs = new ArrayList<>(compacted.size());

            Iterator<JsonValue> expandedProofIterator = expanded.iterator();

            // read proofs
            for (final JsonObject compactedProof : compacted) {

//                final Collection<String> proofTypes = JsonLdType.strings(jsonProof);
//
//                if (proofTypes == null || proofTypes.size() != 1) {
//                    throw new DocumentError(ErrorType.Invalid, "ProofType");
//                }

                JsonObject expandedProof = EmbeddedProof.expandedProof(expandedProofIterator.next());

                if (expandedProof == null) {
                    throw new DocumentError(ErrorType.Invalid, VcdmVocab.PROOF);
                }

                // FIXME
                VerifiableMaterial proofMaterial = new GenericMaterial(
                        Collections.emptyList(),
                        compactedProof,
                        expandedProof.asJsonObject());

                Proof proof = null;

                // find a suite that can materialize the proof
//                for (SignatureSuite suite : suites) {
//                    if (suite.isSupported(verifiable, proofMaterial)) {
//                        proof = suite.getProof(verifiable, proofMaterial, loader);
//                        if (proof != null) {
//                            break;
//                        }
//                    }
//                }

                // process as a generic, i.e. an unknown, proof
                if (proof == null) {
                    var proofTree = reader.read(Json.createArrayBuilder().add(expandedProof.asJsonObject()).build());
//                    proof = GenericProof.of(proofTree.fragment());
                }

                // add a proof to the list
                proofs.add(proof);
            }
            return proofs;

//        } catch (FragmentPropertyError e) {
//            throw DocumentError.of(e);

        } catch (TreeBuilderError e) {
            throw new DocumentError(e, ErrorType.Invalid, "Proof");
        }
    }

    protected Collection<Credential> getCredentials(
            final Collection<String> context,
            final JsonValue value,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        if (JsonUtils.isNull(value)) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.VERIFIABLE_CREDENTIALS);
        }
        if (JsonUtils.isScalar(value)) {
            throw new DocumentError(ErrorType.Invalid, VcdmVocab.VERIFIABLE_CREDENTIALS);
        }

        final Collection<JsonValue> container = JsonUtils.toCollection(value);

        if (container.isEmpty()) {
            return Collections.emptyList();
        }

        final Collection<Credential> credentials = new ArrayList<>(container.size());

        for (final JsonValue item : container) {
            if (JsonUtils.isNotObject(item)) {
                throw new DocumentError(ErrorType.Invalid, "Credential");
            }

            final VerifiableReader credentialReader = resolve(context);

            if (credentialReader == null) {
                throw new DocumentError(ErrorType.Unknown, "Credential");
            }

//            final Verifiable verifiable = credentialReader.read(
//                    JsonLdContext.strings(item.asJsonObject(), context),
//                    item.asJsonObject(),
//                    loader,
//                    base);
//
//            if (!(verifiable instanceof Credential)) {
//                throw new DocumentError(ErrorType.Invalid, "Credential");
//            }
//            credentials.add((Credential) verifiable);
        }
        return credentials;
    }

    protected abstract VerifiableReader resolve(Collection<String> context) throws DocumentError;

    protected boolean isCredential(Collection<String> compactedType) {
        return compactedType.contains(VcdmVocab.CREDENTIAL_TYPE.name());
    }

    protected boolean isPresentation(Collection<String> compactedType) {
        return compactedType.contains(VcdmVocab.PRESENTATION_TYPE.name());
    }

    protected static Collection<String> compactedType(JsonObject document) {
        JsonValue value = document.get("type");
        if (JsonUtils.isNotNull(value)) {
            return JsonUtils.toJsonArray(value).stream()
                    .filter(JsonUtils::isString)
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .toList();
        }
        return Collections.emptySet();
    }
}
