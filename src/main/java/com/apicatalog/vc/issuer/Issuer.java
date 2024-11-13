package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface Issuer {

    /**
     * Signs VC/VP document. Returns the provided VC/VP with an new proof
     *
     * @param documentLocation
     * @param draft            a draft of the proof to sign and attach
     *
     * @return {@link JsonObject} representing the signed document
     *
     * @throws DocumentError
     * @throws SigningError
     */
    JsonObject sign(URI documentLocation, ProofDraft draft) throws SigningError, DocumentError;

    /**
     * Signs VC/VP document. Returns the provided VC/VP with a new proof
     *
     * @param document
     * @param draft    a draft of the proof to sign and attach
     *
     * @return {@link JsonObject} representing the signed document
     *
     * @throws DocumentError
     * @throws SigningError
     */
    JsonObject sign(JsonObject document, ProofDraft draft) throws SigningError, DocumentError;

    /**
     * If set, this overrides the input document's IRI.
     *
     * @param base
     * @return the issuer instance
     */
    Issuer base(URI base);

    /**
     * Set custom loader.
     * 
     * @param loader
     * @return the issuer instance
     */
    Issuer loader(DocumentLoader loader);

    /**
     * Use well-known contexts that are bundled with the library instead of fetching
     * it online. <code>true</code> by default. Disabling might cause slower
     * processing.
     *
     * @param enable
     * @return the issuer instance
     */
    Issuer useBundledContexts(boolean enable);
}
