package com.apicatalog.vc.model;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonObject;

@FunctionalInterface
public interface VerifiableReader {

    /**
     * Materialize VC/VP document into a verifiable instance.
     * 
     * @param document a document to materialize
     * @param loader   a document loader
     * @param base     a base URL, might be null
     * @return a new object representing a credential or a presentation
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    Verifiable read(
            JsonObject document,
            DocumentLoader loader,
            URI base) throws DocumentError;
}
