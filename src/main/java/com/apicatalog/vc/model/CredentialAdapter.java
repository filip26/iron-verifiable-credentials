package com.apicatalog.vc.model;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Credential;

/**
 * Materialize a verifiable credential.
 */
@FunctionalInterface
public interface CredentialAdapter {

    Credential materialize(VerifiableMaterial data, DocumentLoader loader, URI base) throws DocumentError;
}
