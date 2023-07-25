package com.apicatalog.vc.model;

import java.util.ArrayList;
import java.util.Collection;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public final class EmbeddedProof {

    protected EmbeddedProof() {
        /* protected */ }

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
    public static final JsonObject addProof(final JsonObject document, final JsonObject proof) {

        final JsonValue propertyValue = document.get(VcVocab.PROOF.uri());

        final JsonArrayBuilder builder = propertyValue == null
                ? Json.createArrayBuilder()
                : Json.createArrayBuilder(JsonUtils.toJsonArray(propertyValue));

        return Json.createObjectBuilder(document)
                .add(VcVocab.PROOF.uri(),
                        builder.add(
                                Json.createObjectBuilder()
                                        .add(Keywords.GRAPH,
                                                Json.createArrayBuilder().add(proof))))
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
    public static Collection<JsonObject> assertProof(final JsonObject document) throws DocumentError {

        final JsonArray proofValue = document.getJsonArray(VcVocab.PROOF.uri());
        
        if (proofValue != null && proofValue.size() > 0) {
            
            final Collection<JsonObject> proofs = new ArrayList<>(proofValue.size());
            
            for (JsonValue proofGraph : proofValue) {
                
                if (JsonUtils.isNull(proofGraph)) {
                    throw new DocumentError(ErrorType.Missing, VcVocab.PROOF);
                }
                if (JsonUtils.isNotObject(proofGraph)) {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.PROOF);
                }
                final JsonValue proof = proofGraph.asJsonObject().get(Keywords.GRAPH);
                
                if (JsonUtils.isNull(proof)) {
                    throw new DocumentError(ErrorType.Missing, VcVocab.PROOF);
                }
                
                if (JsonUtils.isNotArray(proof) 
                        || proof.asJsonArray().size() != 1
                        || JsonUtils.isNotObject(proof.asJsonArray().get(0))
                        ) {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.PROOF);
                }
                proofs.add(proof.asJsonArray().getJsonObject(0));
            }
            if (proofs.size() > 0) {
                return proofs;
            }
        }
        
        throw new DocumentError(ErrorType.Missing, VcVocab.PROOF);
    }

    public static JsonObject removeProof(final JsonObject document) {
        return Json.createObjectBuilder(document).remove(VcVocab.PROOF.uri()).build();
    }
}
