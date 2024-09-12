package com.apicatalog.vcdm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.builder.GenericTreeCloner;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.traversal.NodeSelector.ProcessingPolicy;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.GenericProof;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public final class EmbeddedProof {

    /**
     * Appends the proof to the given VC/VP document. If the document has been
     * signed already then the proof is added into a proof set.
     *
     * @param document VC/VP document
     * @param proof
     *
     * @return the given VC/VP with the proof attached
     *
     *
     */
    @Deprecated
    public static final JsonObject addProof(final JsonObject document, final JsonObject proof) {

        final JsonValue propertyValue = document.get(VcdmVocab.PROOF.uri());

        final JsonArrayBuilder builder = propertyValue == null
                ? Json.createArrayBuilder()
                : Json.createArrayBuilder(JsonUtils.toJsonArray(propertyValue));

        return Json.createObjectBuilder(document)
                .add(VcdmVocab.PROOF.uri(),
                        builder.add(
                                Json.createObjectBuilder()
                                        .add(Keywords.GRAPH,
                                                Json.createArrayBuilder().add(proof))))
                .build();
    }

    @Deprecated
    public static final JsonObject setProofs(final JsonObject document, final Collection<JsonObject> proofs) {

        final JsonArrayBuilder builder = Json.createArrayBuilder();

        proofs.stream().map(proof -> Json.createObjectBuilder()
                .add(Keywords.GRAPH,
                        Json.createArrayBuilder().add(proof)))
                .forEach(builder::add);

        return Json.createObjectBuilder(document)
                .add(VcdmVocab.PROOF.uri(), builder)
                .build();
    }

    /**
     * Returns a proof set or throws an error if there is no proof.
     * 
     * @param document a {@link JsonObject} representing an serialized verifiable
     *                 credential in an expanded form
     * @return non-empty collection of proofs attached to the given verifiable
     *         credentials. never <code>null</code> nor an empty collection
     * @throws DocumentError if there is no single proof
     */
    @Deprecated
    public static Collection<JsonObject> assertProof(final JsonObject document) throws DocumentError {

        final JsonArray proofValue = document.getJsonArray(VcdmVocab.PROOF.uri());

        if (proofValue != null && proofValue.size() > 0) {

            final Collection<JsonObject> proofs = new ArrayList<>(proofValue.size());

            for (JsonValue proofGraph : proofValue) {

                if (JsonUtils.isNull(proofGraph)) {
                    throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF);
                }
                if (JsonUtils.isNotObject(proofGraph)) {
                    throw new DocumentError(ErrorType.Invalid, VcdmVocab.PROOF);
                }
                final JsonValue proof = proofGraph.asJsonObject().get(Keywords.GRAPH);

                if (JsonUtils.isNull(proof)) {
                    throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF);
                }

                if (JsonUtils.isNotArray(proof)
                        || proof.asJsonArray().size() != 1
                        || JsonUtils.isNotObject(proof.asJsonArray().get(0))) {
                    throw new DocumentError(ErrorType.Invalid, VcdmVocab.PROOF);
                }
                proofs.add(proof.asJsonArray().getJsonObject(0));
            }
            if (proofs.size() > 0) {
                return proofs;
            }
        }

        throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF);
    }

    public static JsonArray getProofs(final JsonObject document) throws DocumentError {
        final JsonValue value = document.get(VcdmVocab.PROOF.uri());
        if (JsonUtils.isNotArray(value)) {
            throw new DocumentError(ErrorType.Invalid, VcdmVocab.PROOF);
        }
        return value.asJsonArray();
    }

    public static JsonObject removeProofs(final JsonObject document) {
        return Json.createObjectBuilder(document).remove(VcdmVocab.PROOF.uri()).build();
    }
    
    /**
     * Creates a new document instance with no proofs attached.
     * 
     * @param verifiable with a proof
     * @return a new document with no proofs
     */
    public static LinkedTree removeProofs(final LinkedTree verifiable) throws DocumentError {
        try {
            var builder = new GenericTreeCloner(verifiable);

            return builder.deepClone(
                    (node, indexOrder, indexTerm, depth) -> VcdmVocab.PROOF.uri().equals(indexTerm)
                            ? ProcessingPolicy.Drop
                            : ProcessingPolicy.Accept);
        } catch (TreeBuilderError e) {
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    public static Collection<Proof> getProofs(final LinkedContainer tree) throws AdapterError {

        Objects.requireNonNull(tree);

        if (tree.nodes().isEmpty()) {
            return Collections.emptyList();
        }

        var proofs = new ArrayList<Proof>();

        for (final LinkedNode node : tree) {
            if (node.isTree()) {
                proofs.add(getProof(node.asTree().fragment()));
                continue;
            }
            proofs.add(getProof(node.asFragment()));
        }

        return proofs;
    }

    public static Proof getProof(final LinkedFragment fragment) throws AdapterError {

        Objects.requireNonNull(fragment);

        if (fragment.type().isAdaptableTo(Proof.class)) {
            return fragment.type().materialize(Proof.class);
        }

        return new GenericProof(fragment);
    }
}
