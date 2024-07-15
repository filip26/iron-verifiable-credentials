package com.apicatalog.vc.reader;

import java.util.Collection;
import java.util.logging.Logger;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.VcVocab;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

/**
 * Materializes an expanded JSON-LD representing a verifiable credential 
 * or presentation.
 * 
 * @since 0.15.0
 */
public class VerifiableWriter {

    private static final Logger LOGGER = Logger.getLogger(VerifiableWriter.class.getName());

    static JsonObject setCredentials(final JsonObject document, final Collection<Credential> credentials) throws DocumentError {

        JsonArrayBuilder builder = Json.createArrayBuilder();
        
//        credentials.stream().map(c -> Json.createObjectBuilder()
//                .add(Keywords.GRAPH,
//                        Json.createArrayBuilder().add(c.expand())))
//                .forEach(builder::add);
        
        return Json.createObjectBuilder(document)
                    .add(VcVocab.VERIFIABLE_CREDENTIALS.uri(), builder).build();
        
    }
}
