package com.apicatalog.vc.proof;

import java.util.Objects;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.model.EmbeddedProof;
import com.apicatalog.vc.model.ProofSignature;

import jakarta.json.JsonObject;

public class StandardProofValue implements ProofSignature {

    protected final DataIntegrityProof proof;
    
    protected byte[] value;

    protected StandardProofValue(DataIntegrityProof proof) {
        this.proof = proof;
    }

    @Override
    public byte[] toByteArray() {
        return value;
    }

    @Override
    public void verify(JsonObject data, VerificationKey method) throws VerificationError {

        Objects.requireNonNull(data);
        Objects.requireNonNull(method);

        final CryptoSuite cryptoSuite = proof.getCryptoSuite();

        if (cryptoSuite == null) {
            throw new VerificationError(Code.UnsupportedCryptoSuite);
        }

        // remote a proof value and a new unsigned copy
        final JsonObject unsignedProof = proof.unsignedCopy();

//        final LinkedDataSignature signature = new LinkedDataSignature(cryptoSuite);

        // verify signature
//        signature.verify(
//                null, //FIXME
//                data,
//                unsignedProof,
//                method.publicKey(),
//                value);

    }

}
