package com.apicatalog.vc.solid;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.issuer.AbstractIssuer;
import com.apicatalog.vc.suite.SignatureSuite;

/**
 * Represent an issuer not supporting a selective disclosure.
 */
public class SolidIssuer extends AbstractIssuer {
    public SolidIssuer(SignatureSuite suite, CryptoSuite crypto, KeyPair keyPair, Multibase proofValueBase) {
        super(suite, crypto, keyPair, proofValueBase);
    }
}
