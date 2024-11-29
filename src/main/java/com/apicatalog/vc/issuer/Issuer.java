package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.controller.method.GenericMethodUri;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.model.DocumentError;

import jakarta.json.JsonObject;

public interface Issuer {

    /**
     * Signs VC/VP document. Returns the provided VC/VP with an new signed proof.
     *
     * @param location a location of a verifiable document to attach a new signed
     *                 proof
     * @param draft    a draft of the proof to sign and attach
     *
     * @return {@link JsonObject} representing the signed document
     *
     * @throws DocumentError
     * @throws CryptoSuiteError
     */
    JsonObject sign(URI location, ProofDraft draft) throws CryptoSuiteError, DocumentError;

    /**
     * Signs VC/VP document. Returns the provided VC/VP with a new signed proof.
     *
     * @param document a verifiable document to attach a new signed proof
     * @param draft    a draft of the proof to sign and attach
     *
     * @return {@link JsonObject} representing the signed document
     *
     * @throws DocumentError
     * @throws CryptoSuiteError
     */
    JsonObject sign(JsonObject document, ProofDraft draft) throws CryptoSuiteError, DocumentError;

    /**
     * If set, it overrides the input document base IRI.
     *
     * @param base
     * @return the issuer instance
     */
    Issuer base(URI base);

    /**
     * Set a custom document loader.
     * 
     * @param loader a loader to fetch JSON-LD contexts and documents
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

    /**
     * A cryptographic suite associated with the issuer.
     * 
     * @return a cryptographic suite, never <code>null</code>
     */
    CryptoSuite cryptosuite();

    /**
     * Create a new proof draft.
     * 
     * @param method a verification method to verify the proof signature value
     * @return a new proof draft instance, never <code>null</code>
     */
    <T extends ProofDraft> T createDraft(VerificationMethod method);

    /**
     * Create a new proof draft.
     * 
     * @param method a verification method to verify the proof signature value
     * @return a new proof draft instance, never <code>null</code>
     */
    default <T extends ProofDraft> T createDraft(URI method) {
        return createDraft(new GenericMethodUri(method));
    }
}
