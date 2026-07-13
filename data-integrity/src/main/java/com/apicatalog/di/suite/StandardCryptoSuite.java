package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.signature.SignatureGenerator;

public abstract class StandardCryptoSuite implements CryptoSuite {

    String id;
    String c14n; // JCS, RDFC, ..
    SignatureGenerator<DataIntegrityProof> signatureGenerator;

    public StandardCryptoSuite(
            String id,
            String c14n,
            SignatureGenerator<DataIntegrityProof> signatureGenerator) {
        this.id = id;
        this.c14n = c14n;
        this.signatureGenerator = signatureGenerator;
    }

    public DataIntegrityProof sign(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof.Builder proofDraft,
            DigestiblePayload payload) throws SignatureException {

        proofDraft.canonize(c14n);

        var unsigned = proofDraft.snapshot();

        var signature = signatureGenerator.generate(
                algorithm,
                signer,
                digestFactory,
                unsigned,
                payload);

        return proofDraft.build(signature);
    }

    public String id() {
        return id;
    }

    public String c14n() {
        return c14n;
    }

}
