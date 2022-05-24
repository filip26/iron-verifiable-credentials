package com.apicatalog.vc;

import java.io.StringReader;
import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.api.ExpansionApi;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.proof.EmbeddedProof;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
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

    static VcDocument load(URI location, DocumentLoader loader) throws DataIntegrityError {
        return load(JsonLd.expand(location), loader);
    }

    static VcDocument load(JsonStructure document, DocumentLoader loader) throws DataIntegrityError {
        return load(JsonLd.expand(JsonDocument.of(document)), loader);
    }

    static VcDocument load(String json, DocumentLoader loader) throws DataIntegrityError {
        try {
            return load(JsonLd.expand(JsonDocument.of(new StringReader(json))), loader);
        } catch (JsonLdError e) {
            throw new DataIntegrityError(e);
        }
    }

    static VcDocument load(ExpansionApi api, DocumentLoader loader) throws DataIntegrityError {
        try {
            // VC/VP in expanded form
            final JsonArray expanded = api.loader(new StaticContextLoader(loader)).get();   //TODO make use of static loader optional

            return VcDocument.from(expanded);

        } catch (JsonLdError e) {
            throw new DataIntegrityError(e);
        }        
    }

    
    static VcDocument from(JsonArray expanded) throws DataIntegrityError {

        if (expanded == null || expanded.isEmpty()) {
            throw new DataIntegrityError();                  //TODO
        }

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
