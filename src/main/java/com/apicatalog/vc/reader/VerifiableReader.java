package com.apicatalog.vc.reader;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonObject;

public interface VerifiableReader {

    /**
     * Read VC/VP document
     * 
     * @param context
     * @param document
     * @param loader
     * @param base
     * @return {@link Verifiable} object representing a credential or a presentation
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    Verifiable read(
            Collection<String> context,
            JsonObject document,
            DocumentLoader loader,
            URI base) throws DocumentError;
}
