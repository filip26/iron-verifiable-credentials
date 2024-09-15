package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.jsonld.JsonLdType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.selector.InvalidSelector;
import com.apicatalog.linkedtree.traversal.NodeSelector.TraversalPolicy;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.proof.GenericProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public abstract class VcdmReader implements VerifiableReader {

    protected final JsonLdTreeReader reader;

    protected final Collection<SignatureSuite> suites;

    public VcdmReader(JsonLdTreeReader reader, final SignatureSuite... suites) {
        this.suites = Arrays.asList(suites);
        this.reader = reader;
    }

    @Override
    public Verifiable read(
            final Collection<String> context,
            final JsonObject document,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        final Collection<String> types = types(document);

        if (isCredential(types)) {
            return readCredential(context, document, loader, base);

        } else if (isPresentation(types)) {
            return readPresentation(context, document, loader, base);
        }
        throw new DocumentError(ErrorType.Unknown, "Verifiable");
    }

    protected static boolean isCredential(Collection<String> types) {
        return types.contains(VcdmVocab.CREDENTIAL_TYPE.name());
    }

    protected static boolean isPresentation(Collection<String> types) {
        return types.contains(VcdmVocab.PRESENTATION_TYPE.name());
    }

    protected static Collection<String> types(JsonObject document) {
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
                    .base(base).get();

            final Presentation presentation = (Presentation) read(
                    context,
                    expanded,
                    loader,
                    base);

            ((Vcdm11Presentation) presentation).credentials = 
                    getCredentials(context, document.get(VcdmVocab.VERIFIABLE_CREDENTIALS.name()), loader, base);

            return presentation;

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Credential readCredential(
            final Collection<String> context,
            final JsonObject document,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .context(Json.createArrayBuilder(context).build())
                    .loader(loader)
                    .base(base).get();

            return (Credential) read(
                    context,
                    expanded,
                    loader,
                    base);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    Verifiable read(
            final Collection<String> context,
            final JsonArray expanded,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        if (expanded == null
                || expanded.size() != 1
                || ValueType.OBJECT != expanded.iterator().next().getValueType()) {
            throw new DocumentError(ErrorType.Invalid);
        }

        try {
            // get a verifiable
            final LinkedTree tree = reader.read(
                    context,
                    expanded,
                    // drop main proofs
                    ((node, indexOrder, indexTerm, depth) -> (VcdmVocab.PROOF.uri().equals(indexTerm)
                            || VcdmVocab.VERIFIABLE_CREDENTIALS.uri().equals(indexTerm))
                                    ? TraversalPolicy.Drop
                                    : TraversalPolicy.Accept));

            if (tree == null
                    || tree.size() != 1
                    || !tree.node().isFragment()
                    || !tree.node().asFragment().type().isAdaptableTo(Verifiable.class)) {
                throw new DocumentError(ErrorType.Invalid, "document");
            }

            final Verifiable verifiable = tree.object(Verifiable.class);

            // detach proofs
            final JsonArray jsonProofs = EmbeddedProof.getProofs(expanded.iterator().next().asJsonObject());

            ((VcdmVerifiable) verifiable).proofs(getProofs(verifiable, jsonProofs, loader));

            return verifiable;

        } catch (DocumentError e) {
            throw e;

        } catch (TreeBuilderError | AdapterError e) {
            throw new DocumentError(e, ErrorType.Invalid, "document");
        }
    }

    protected Collection<Proof> getProofs(Verifiable verifiable, JsonArray jsonProofs, DocumentLoader loader) throws DocumentError {
        try {
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
            return proofs;

        } catch (InvalidSelector e) {
            throw DocumentError.of(e);

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

        for (JsonValue item : container) {
            if (JsonUtils.isNotObject(item)) {
                throw new DocumentError(ErrorType.Invalid, "Credential");
            }
            
            VerifiableReader credentialReader = resolve(context);
            
            if (credentialReader == null) {
                throw new DocumentError(ErrorType.Unknown, "Credential");
            }
            
            Verifiable verifiable = credentialReader.read(context, item.asJsonObject(), loader, base);
            
            if (!(verifiable instanceof Credential)) {
                throw new DocumentError(ErrorType.Invalid, "Credential");
            }
            credentials.add((Credential)verifiable);
        }
        return credentials;
    }
    
    protected abstract VerifiableReader resolve(Collection<String> context) throws DocumentError;

}
