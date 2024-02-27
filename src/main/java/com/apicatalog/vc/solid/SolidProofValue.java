package com.apicatalog.vc.solid;

import java.util.Objects;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

/**
 * Represent a proof value used together with full disclosure suites. i.e.
 * suites do not allowing a selective disclosure.
 */
public class SolidProofValue implements ProofValue {

    protected final byte[] value;

    public SolidProofValue(byte[] value) {
        this.value = value;
    }

    @Override
    public void verify(CryptoSuite cryptoSuite, JsonStructure context, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError {

        Objects.requireNonNull(value);
        Objects.requireNonNull(data);
        Objects.requireNonNull(publicKey);

        if (cryptoSuite == null) {
            throw new VerificationError(Code.UnsupportedCryptoSuite);
        }

        final LinkedDataSignature signature = new LinkedDataSignature(cryptoSuite);

        // verify signature
        signature.verify(
                data,
                unsignedProof,
                publicKey,
                value);
    }

    @Override
    public byte[] toByteArray() {
        return value;
    }
}
