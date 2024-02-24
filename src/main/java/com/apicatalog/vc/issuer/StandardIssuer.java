package com.apicatalog.vc.issuer;

import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class StandardIssuer<T extends ProofValue> extends BaseIssuer<T> {

    protected StandardIssuer(KeyPair keyPair, SignatureSuite suite) {
        super(keyPair);
    }

    @Override
    public void sign(JsonStructure context, JsonObject data, Proof<T> draft) throws SigningError {

        final LinkedDataSignature ldSignature = new LinkedDataSignature(draft.cryptoSuite());

        final JsonObject unsignedDraft = draft.expand();

        final byte[] signature = ldSignature.sign(data, keyPair.privateKey(), unsignedDraft);

        draft.signature(signature);
    }
}
