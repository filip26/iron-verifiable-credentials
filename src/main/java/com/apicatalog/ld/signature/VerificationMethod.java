package com.apicatalog.ld.signature;

import java.net.URI;

/**
 * Represents proof verification method declaration.
 *
 * @see <a href=
 *      "https://w3c-ccg.github.io/data-integrity-spec/#verification-methods">Verification
 *      Methods</a>
 *
 */
public interface VerificationMethod {

    URI id();

    URI type();

    URI controller();

}