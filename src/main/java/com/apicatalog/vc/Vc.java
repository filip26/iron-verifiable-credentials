package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.VerificationError.Type;

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

                // verify embedded proof
                final Proof proof = EmbeddedProof.verify(verifiable, null);     //FIXME pass verification result

                // verify supported crypto suite
                if (!proof.isTypeOf("https://w3id.org/security#Ed25519Signature2020")) {
                    throw new VerificationError(Type.UnknownCryptoSuiteType);
                }

                // verify supported proof value encoding
                if (!proof.getValue().isTypeOf("https://w3id.org/security#multibase")) {
                    throw new VerificationError(Type.InvalidProofValue);
                }

                // verify proof value
                if (!Multibase.isAlgorithmSupported(proof.getValue().getValue())) {
                    throw new VerificationError(Type.InvalidProofValue);
                }
                
                // decode proof value
                byte[] proofValue = Multibase.decode(proof.getValue().getValue());
                
                // verify proof value length
                if (proofValue.length != 64) {
                    throw new VerificationError(Type.InvalidProofLenght);
                }

                
                //TODO
            }


            // TODO Auto-generated method stub
            return null;

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }

}
