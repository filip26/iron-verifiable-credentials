package com.apicatalog.ld.signature.jws;

import com.apicatalog.ld.DocumentError;
//import com.apicatalog.ld.signature.proof.ProofAdapter;
//import com.apicatalog.ld.signature.proof.VerificationMethodAdapter;

import com.apicatalog.ld.signature.jws.from_lib_v070.VerificationMethodAdapter;
import jakarta.json.JsonObject;

/**
 * Based on com.apicatalog.ld.signature.proof.ProofAdapter
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public interface JwsProofAdapter {

    String type();

    JwsProof deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(JwsProof proof) throws DocumentError;

    JsonObject setProofValue(JsonObject proof, String jws) throws DocumentError;

    VerificationMethodAdapter getMethodAdapter();

}
