package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public interface Issuer {

    /**
     * Get signed document in expanded form.
     *
     * @return the signed document in expanded form
     *
     * @throws SigningError
     * @throws DocumentError
     */
    SignedCredentials sign(URI input, DataIntegrityProof draft) throws SigningError, DocumentError;

    SignedCredentials sign(JsonObject document, Proof draft) throws SigningError, DocumentError;

}
