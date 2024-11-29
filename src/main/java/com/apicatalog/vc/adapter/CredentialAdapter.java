package com.apicatalog.vc.adapter;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.VerifiableMaterial;

/**
 * Materialize a verifiable credential.
 */
@FunctionalInterface
public interface CredentialAdapter {

    Credential materialize(VerifiableMaterial data, DocumentLoader loader, URI base) throws DocumentError;
}
