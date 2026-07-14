package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureGenerator;

public abstract class StandardCryptoSuite implements CryptoSuite {

    protected final String id;
    protected final String c14n; // JCS, RDFC, .
    protected final Multibase multibase;
    protected final SignatureGenerator<DataIntegrityProof> signatureGenerator;

    public StandardCryptoSuite(
            String id,
            String c14n,
            Multibase multibase,
            SignatureGenerator<DataIntegrityProof> signatureGenerator) {
        this.id = id;
        this.c14n = c14n;
        this.multibase = multibase;
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

    @Override
    public String encode(Signature signature) {
        return multibase.encode(signature.toByteArray());
    }

    @Override
    public Signature decode(String encoded, Proof proof, PayloadProcessor payload) {
        var signature = multibase.decode(encoded);
        return decode(signature, proof, payload);
    }

    protected abstract Signature decode(byte[] signature, Proof proof, PayloadProcessor payload);

    public String id() {
        return id;
    }

    public String c14n() {
        return c14n;
    }

}
