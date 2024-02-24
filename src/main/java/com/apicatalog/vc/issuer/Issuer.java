package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.model.Verifiable;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.JsonObject;

public interface Issuer<T extends ProofValue> {

    /**
     * Get signed document in expanded form.
     *
     * @return the signed document in expanded form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    Verifiable sign(URI input, DataIntegrityProof<T> draft) throws SigningError, DocumentError;

    Verifiable sign(JsonObject document, Proof<T> draft) throws SigningError, DocumentError;

    /**
     * If set, this overrides the input document's IRI.
     *
     * @param base
     * @return the processor instance
     */
    <I extends Issuer<T>> I base(URI base);
    
    <I extends Issuer<T>> I loader(DocumentLoader loader);
    
    /**
     * Use well-known contexts that are bundled with the library instead of fetching
     * it online. <code>true</code> by default. Disabling might cause slower
     * processing.
     *
     * @param enable
     * @return the processor instance
     */
    <I extends Issuer<T>> I useBundledContexts(boolean enable);
}
