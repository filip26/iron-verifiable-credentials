package com.apicatalog.vc.solid;

import java.util.function.Function;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.issuer.AbstractIssuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.suite.SignatureSuite;

/**
 * Represent an issuer not supporting a selective disclosure.
 */
public class SolidIssuer extends AbstractIssuer {
    public SolidIssuer(
            SignatureSuite suite, 
            CryptoSuite crypto, 
            KeyPair keyPair, 
            Multibase proofValueBase, 
            Function<VerificationMethod, ? extends ProofDraft> proofDraftProvider) {
        super(suite, crypto, keyPair, proofValueBase, proofDraftProvider);
    }
}
