package com.apicatalog.ld.signature;

import java.net.URI;

import com.apicatalog.linkedtree.Linkable;

/**
 * Represents proof verification method declaration.
 */
public interface VerificationMethod extends Linkable {

    URI id();

    URI type();

    URI controller();

}