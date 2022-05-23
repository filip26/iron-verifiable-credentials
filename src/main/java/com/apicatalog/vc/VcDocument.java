package com.apicatalog.vc;

import com.apicatalog.jsonld.json.JsonUtils;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public interface VcDocument {

    default boolean isVerifiable() {
        return false;
    }

    default boolean isCredentials() {
        return false;
    }

    default boolean isPresentation() {
        return false;
    }

    default Verifiable asVerifiable() {
        return isVerifiable() ? (Verifiable)this : null;
    }

    default Credentials asCredentials() {
        return isCredentials() ? (Credentials)this : null;
    }

    default Presentation asPresentation() {
        return isPresentation() ? (Presentation)this : null;
    }

    static VcDocument from(JsonArray expanded) throws DataIntegrityError {
        
        for (final JsonValue item : expanded) {

            if (JsonUtils.isNotObject(item)) {
                //TODO warning
                continue;
            }

            final JsonObject verifiable = item.asJsonObject();

            //TODO VC or VP ?

            // verify embedded proof
            final Proof proof = EmbeddedProof.from(verifiable);
            
            //TODO
         
            return new ImmutableVerifiableCredentials(null, proof);
        }
        
        throw new IllegalStateException();
    }
}
