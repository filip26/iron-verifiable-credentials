package com.apicatalog.vc.model;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

@FunctionalInterface
public interface VerifiableMaterialReader {

    /**
     * Read verifiable material.
     * 
     * @param document a document to read
     * @param loader   a document loader
     * @param base     a base URL, might be null
     * @return a verifiable material instance
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    VerifiableMaterial read(
            JsonObject document,
            DocumentLoader loader,
            URI base) throws DocumentError;
}
