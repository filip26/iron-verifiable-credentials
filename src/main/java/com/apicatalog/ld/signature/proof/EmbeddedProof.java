package com.apicatalog.ld.signature.proof;

import java.util.Collection;

import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;

import jakarta.json.Json;
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

        final JsonValue propertyValue = document.get(VcVocab.PROOF.id());

        return Json.createObjectBuilder(document)
                .add(VcVocab.PROOF.id(),
                        ((propertyValue != null)
                                ? Json.createArrayBuilder(JsonUtils.toJsonArray(propertyValue))
                                : Json.createArrayBuilder()).add(proof))
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
    public static Collection<JsonValue> assertProof(final JsonObject document) throws DocumentError {

        final Collection<JsonValue> proofs = JsonLdReader.getObjects(document, VcVocab.PROOF.id());

        if (proofs == null || proofs.size() == 0) {
            throw new DocumentError(ErrorType.Missing, VcVocab.PROOF);
        }
        return proofs;
    }

    public static JsonObject removeProof(final JsonObject document) {
        return Json.createObjectBuilder(document).remove(VcVocab.PROOF.id()).build();
    }
}
