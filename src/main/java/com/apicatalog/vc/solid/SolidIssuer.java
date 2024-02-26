package com.apicatalog.vc.solid;

import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.integrity.DataIntegrityProofDraft;
import com.apicatalog.vc.issuer.AbstractIssuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Represent an issuer does not allowing a selective disclosure.
 */
public class SolidIssuer extends AbstractIssuer {

    public SolidIssuer(SignatureSuite suite, KeyPair keyPair, Multibase proofValueBase) {
        super(suite, keyPair, proofValueBase);
    }

    @Override
    protected JsonObject sign(JsonArray context, JsonObject document, ProofDraft draft) throws SigningError {
        final JsonObject unsignedDraft = draft.unsigned();

        final LinkedDataSignature ldSignature = new LinkedDataSignature(draft.cryptoSuite());

        final byte[] signature = ldSignature.sign(document, keyPair.privateKey(), unsignedDraft);

        final JsonObject proofValue = LdScalar.multibase(proofValueBase, signature);

        // signed proof
        return DataIntegrityProofDraft.signed(draft.unsigned(), proofValue);
    }
}