package com.apicatalog.vc.issuer;

import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

public class SolidIssuer extends AbstractIssuer {

    public SolidIssuer(SignatureSuite suite, KeyPair keyPair, Multibase proofValueBase) {
        super(suite, keyPair, proofValueBase);
    }

    @Override
    protected JsonObject sign(JsonArray context, JsonObject document, Proof draft) throws SigningError {
        final JsonObject unsignedDraft = draft.unsignedCopy();

        final LinkedDataSignature ldSignature = new LinkedDataSignature(draft.cryptoSuite());

        final byte[] signature = ldSignature.sign(document, keyPair.privateKey(), unsignedDraft);

        return LdScalar.multibase(proofValueBase, signature);
    }
}
