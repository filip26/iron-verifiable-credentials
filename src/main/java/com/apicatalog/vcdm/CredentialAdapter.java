package com.apicatalog.vcdm;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.model.VerifiableMaterial;

/**
 * Materialize verifiable presentation credentials.
 */
@FunctionalInterface
public interface CredentialAdapter {

    Credential materialize(VerifiableMaterial data, DocumentLoader loader, URI base) throws DocumentError;
}
