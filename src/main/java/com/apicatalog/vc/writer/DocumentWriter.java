package com.apicatalog.vc.writer;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.VerifiableDocument;

import jakarta.json.JsonObject;

public interface DocumentWriter {

    /**
     * Write VC/VP document
     * 
     * @param verifiable
     * @param loader
     * @param base
     * @return {@link JsonObject} object representing a compacted credential or a
     *         compacted presentation
     * 
     * @throws DocumentError if the document cannot be written
     * 
     */
    JsonObject write(
            VerifiableDocument verifiable,
            DocumentLoader loader,
            URI base) throws DocumentError;

}
