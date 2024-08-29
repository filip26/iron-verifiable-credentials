package com.apicatalog.ld.signature;

import java.net.URI;

import com.apicatalog.linkedtree.Linkable;

/**
 * Represents proof verification method declaration.
 *
 * @see <a href=
 *      "https://w3c-ccg.github.io/data-integrity-spec/#verification-methods">Verification
 *      Methods</a>
 *
 */
public interface VerificationMethod extends Linkable {

    URI id();

    URI type();

    URI controller();

}