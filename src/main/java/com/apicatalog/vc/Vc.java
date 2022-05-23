package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.loader.DocumentLoader;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * High level API to process Verified Credentials and Verified Presentations
 *
 */
public final class Vc {

    public static VerificationResult verify(String location, DocumentLoader loader) throws VerificationError {

        try {
            // VC/VP in expanded form
            final JsonArray expanded = JsonLd.expand(location).loader(loader).get();

            if (expanded == null || expanded.isEmpty()) {
                //TODO error
                return null;
            }

            for (final JsonValue item : expanded) {

                if (!ValueType.OBJECT.equals(item.getValueType())) {
                    //TODO warning
                    continue;
                }

                final JsonObject verifiable = item.asJsonObject();

                //TODO VC or VP ?

                //TODO data integrity check

                // verify embedded proof
                final Proof proof = EmbeddedProof.verify(verifiable, null);     //FIXME pass verification result

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
