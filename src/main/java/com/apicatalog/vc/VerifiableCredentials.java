package com.apicatalog.vc;

import java.io.StringReader;
import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.api.ExpansionApi;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.lds.proof.EmbeddedProof;
import com.apicatalog.lds.proof.Proof;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

/**
 * A verifiable credential is a set of tamper-evident claims
 * and metadata that cryptographically prove who issued it.
 */
public interface VerifiableCredentials extends Verifiable, Credentials {


    static VerifiableCredentials load(URI location, DocumentLoader loader) throws DataIntegrityError {
        return load(JsonLd.expand(location), loader);
    }

    static VerifiableCredentials load(JsonStructure document, DocumentLoader loader) throws DataIntegrityError {
        return load(JsonLd.expand(JsonDocument.of(document)), loader);
    }

    static VerifiableCredentials load(String json, DocumentLoader loader) throws DataIntegrityError {
        try {
            return load(JsonLd.expand(JsonDocument.of(new StringReader(json))), loader);
        } catch (JsonLdError e) {
            throw new DataIntegrityError(e);
        }
    }

    static VerifiableCredentials load(ExpansionApi api, DocumentLoader loader) throws DataIntegrityError {
        try {
            // VC/VP in expanded form
            final JsonArray expanded = api.loader(new StaticContextLoader(loader)).get();   //TODO make use of static loader optional

            return from(expanded, loader);

        } catch (JsonLdError e) {
            throw new DataIntegrityError(e);
        }        
    }

    
    static VerifiableCredentials from(JsonArray expanded, DocumentLoader loader) throws DataIntegrityError {

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
            final Proof proof = EmbeddedProof.from(verifiable, loader);
            
            //TODO
         
            return new ImmutableVerifiableCredentials(null, proof, expanded);
        }
        
        throw new IllegalStateException();
    }
    
}
