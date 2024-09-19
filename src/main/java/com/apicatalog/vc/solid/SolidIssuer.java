package com.apicatalog.vc.solid;

import com.apicatalog.controller.method.KeyPair;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.issuer.AbstractIssuer;
import com.apicatalog.vc.issuer.ProofDraft;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Represent an issuer not supporting a selective disclosure.
 */
public class SolidIssuer extends AbstractIssuer {

    public SolidIssuer(CryptoSuite crypto, KeyPair keyPair, Multibase proofValueBase) {
        super(crypto, keyPair, proofValueBase);
    }

    @Override
    protected byte[] sign(JsonArray context, LinkedTree document, ProofDraft draft) throws SigningError {
        final JsonObject unsignedDraft = draft.unsigned();

        final LinkedDataSignature ldSignature = new LinkedDataSignature(crypto);

//FIXME        return ldSignature.sign(document, keyPair.privateKey(), unsignedDraft);
        return null;
    }
}
