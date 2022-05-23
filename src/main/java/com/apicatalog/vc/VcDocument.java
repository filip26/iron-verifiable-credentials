package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.proof.EmbeddedProof;
import com.apicatalog.vc.proof.Proof;

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

    static VcDocument load(String location, DocumentLoader loader) throws DataIntegrityError {
        try {
            // VC/VP in expanded form
            final JsonArray expanded = JsonLd.expand(location).loader(loader).get();

            if (expanded == null || expanded.isEmpty()) {
                throw new DataIntegrityError();                  //TODO
            }

            return VcDocument.from(expanded);

        } catch (JsonLdError e) {
            throw new DataIntegrityError(e);
        }
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
