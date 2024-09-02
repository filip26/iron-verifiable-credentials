package com.apicatalog.vc.jsonld;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonObject;

public interface JsonLdVerifiableReader {

    /**
     * Reads VC/VP document.
     * 
     * @param document
     * @param loader
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    Verifiable read(final JsonObject document, DocumentLoader loader) throws DocumentError;
}
