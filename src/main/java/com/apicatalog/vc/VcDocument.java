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
        return (Verifiable)this;
    }

    default Credentials asCredentials() {
        return (Credentials)this;
    }

    default Presentation asPresentation() {
        return (Presentation)this;
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
