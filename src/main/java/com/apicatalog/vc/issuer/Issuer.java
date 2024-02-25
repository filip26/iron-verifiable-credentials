package com.apicatalog.vc.issuer;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.vc.proof.ProofDraft;

import jakarta.json.JsonObject;

public interface Issuer {

    /**
     * Signs VC/VP document. Returns the provided VC/VP with an new proof
     *
     * @param documentLocation
     * @param draft            a draft of the proof to sign and attach
     *
     * @return {@link IssuedVerifiable} representing the signed document
     *
     * @throws DocumentError
     * @throws SigningError
     */
    IssuedVerifiable sign(URI documentLocation, ProofDraft draft) throws SigningError, DocumentError;

    /**
     * Signs VC/VP document with a proof allowing a selective disclosure of claims.
     *
     * @param documentLocation
     * @param draft            a draft of the proof to sign and attach
     * @param selectors        JSON pointers specifying mandatory claims that are
     *                         always disclosed.
     *
     * @return {@link IssuedVerifiable} representing the signed document
     *
     * @throws DocumentError
     * @throws SigningError
     * @throws UnsupportedOperationException if a suite does not support a selective
     *                                       disclosure
     */
    default IssuedVerifiable sign(URI documentLocation, ProofDraft draft, Collection<String> selectors) throws SigningError, DocumentError {
        throw new UnsupportedOperationException();
    }

    /**
     * Signs VC/VP document. Returns the provided VC/VP with a new proof
     *
     * @param document
     * @param draft    a draft of the proof to sign and attach
     *
     * @return {@link IssuedVerifiable} representing the signed document
     *
     * @throws DocumentError
     * @throws SigningError
     */
    IssuedVerifiable sign(JsonObject document, ProofDraft draft) throws SigningError, DocumentError;

    /**
     * Signs VC/VP document with a proof allowing a selective disclosure of claims.
     *
     * @param document
     * @param draft     a draft of the proof to sign and attach
     * @param selectors JSON pointers specifying mandatory claims that are always
     *                  disclosed.
     *
     * @return {@link IssuedVerifiable} representing the signed document
     *
     * @throws DocumentError
     * @throws SigningError
     * @throws UnsupportedOperationException if a suite does not support a selective
     *                                       disclosure
     */
    default IssuedVerifiable sign(JsonObject document, ProofDraft draft, Collection<String> selectors) throws SigningError, DocumentError {
        throw new UnsupportedOperationException();
    }

    /**
     * If set, this overrides the input document's IRI.
     *
     * @param base
     * @return the processor instance
     */
    Issuer base(URI base);

    Issuer loader(DocumentLoader loader);

    /**
     * Use well-known contexts that are bundled with the library instead of fetching
     * it online. <code>true</code> by default. Disabling might cause slower
     * processing.
     *
     * @param enable
     * @return the processor instance
     */
    Issuer useBundledContexts(boolean enable);
}
