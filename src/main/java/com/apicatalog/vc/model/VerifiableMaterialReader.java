package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface VerifiableMaterialReader {

    /**
     * Read verifiable material.
     * 
     * @param context  an injected JSON-LD context or an empty collection
     * @param document a document to read
     * @param loader   a document loader
     * @param base     a base URL, might be null
     * @return a verifiable material instance
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    VerifiableMaterial read(
            Collection<String> context,
            JsonObject document,
            DocumentLoader loader,
            URI base) throws DocumentError;
}
