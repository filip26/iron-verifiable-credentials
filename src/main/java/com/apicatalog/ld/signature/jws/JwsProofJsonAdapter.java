package com.apicatalog.ld.signature.jws;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.json.VerificationMethodJsonAdapter;
import jakarta.json.JsonObject;

/**
 * Based on com.apicatalog.ld.signature.proof.ProofAdapter
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public interface JwsProofJsonAdapter {

    String type();

    JwsProof deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(JwsProof proof) throws DocumentError;

    JsonObject setProofValue(JsonObject proof, String jws) throws DocumentError;

    VerificationMethodJsonAdapter getMethodAdapter();

}
