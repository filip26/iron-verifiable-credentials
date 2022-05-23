package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.multibase.Multibase;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * High level API to process Verified Credentials and Verified Presentations
 *
 */
public final class Vc {

    public static VerificationResult verify(String location, DocumentLoader loader) throws VerificationError, DataIntegrityError {

        try {
            // VC/VP in expanded form
            final JsonArray expanded = JsonLd.expand(location).loader(loader).get();

            if (expanded == null || expanded.isEmpty()) {
                //TODO error
                return null;
            }

            for (final JsonValue item : expanded) {

                if (JsonUtils.isNotObject(item)) {
                    //TODO warning
                    continue;
                }

                final JsonObject verifiable = item.asJsonObject();

                //TODO VC or VP ?

                //TODO data integrity check

                // verify embedded proof
                final Proof proof = EmbeddedProof.verify(verifiable, null);     //FIXME pass verification result

                // verify supported crypto suite
                if (!proof.isTypeOf("https://w3id.org/security#Ed25519Signature2020")) {
                    //TODO UNKNOWN_CRYPTOSUITE_TYPE code
                    throw new VerificationError();      // an unknown crypto suite
                }

                // verify supported proof value encoding
                if (!proof.getValue().isTypeOf("https://w3id.org/security#multibase")) {
                    //TODO NVALID_PROOF_VALUE code
                    throw new VerificationError();
                }

                // decode proof value
                if (!Multibase.isAlgorithmSupported(proof.getValue().getValue())) {
                    //TODO NVALID_PROOF_VALUE code
                    throw new VerificationError();
                }
                
                byte[] proofValue = Multibase.decode(proof.getValue().getValue());
                if (proofValue.length != 64) {
                    //TODO INVALID_PROOF_LENGTH code
                    throw new VerificationError();
                }
                                
                //TODO
            }


            // TODO Auto-generated method stub
            return null;

        } catch (JsonLdError e) {
            e.printStackTrace();
            throw new VerificationError();
        }
    }

}
