package com.apicatalog.vc.solid;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.key.KeyPair;
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
    protected byte[] sign(JsonArray context, JsonObject document, ProofDraft draft) throws SigningError {
        final JsonObject unsignedDraft = draft.unsigned();

        final LinkedDataSignature ldSignature = new LinkedDataSignature(crypto);

        return ldSignature.sign(document, keyPair.privateKey(), unsignedDraft);
    }
}
