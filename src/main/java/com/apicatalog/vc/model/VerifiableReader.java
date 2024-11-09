package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonObject;

public interface VerifiableReader {

    /**
     * Materialize VC/VP document into a verifiable instance.
     * 
//     * @param context
     * @param document a document to materialize
     * @param loader   a document loader
     * @param base     a base URL, might be null
     * @return a new object representing a credential or a presentation
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    Verifiable read(
//            Collection<String> context,
            JsonObject document,
            DocumentLoader loader,
            URI base) throws DocumentError;
}
