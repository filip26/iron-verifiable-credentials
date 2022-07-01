package com.apicatalog.did;

/**
 * Performs {@link Did} resolution by expanding {@link Did} into {@link DidDocument}.
 *
 * @see {@link <a href="https://www.w3.org/TR/did-core/#dfn-did-resolvers">DID resolvers</a>}
 */
public interface DidResolver {

    /**
     * Resolves the given {@link Did} into {@link DidDocument}
     *
     * @param did To resolve
     * @return The new {@link DidDocument}
     */
    DidDocument resolve(Did did);
}
