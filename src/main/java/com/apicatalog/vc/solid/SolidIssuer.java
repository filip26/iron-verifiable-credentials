package com.apicatalog.vc.solid;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.issuer.AbstractIssuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;

/**
 * Represent an issuer not supporting a selective disclosure.
 */
public class SolidIssuer extends AbstractIssuer {

    public SolidIssuer(SignatureSuite suite, CryptoSuite crypto, KeyPair keyPair, Multibase proofValueBase) {
        super(suite, crypto, keyPair, proofValueBase);
    }

//    @Override
//    protected byte[] sign(JsonArray context, LinkedTree document, ProofDraft draft) throws SigningError {
////        final JsonObject unsignedDraft = draft.unsigned();
//
////        final Signature ldSignature = new Signature(crypto);
//
////FIXME        return ldSignature.sign(document, keyPair.privateKey(), unsignedDraft);
//        return null;
//    }
}
