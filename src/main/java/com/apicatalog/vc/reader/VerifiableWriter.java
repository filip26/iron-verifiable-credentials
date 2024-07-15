package com.apicatalog.vc.reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.uri.UriUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.ld.Term;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

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
