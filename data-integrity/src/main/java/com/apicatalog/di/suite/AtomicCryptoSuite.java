package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureDecoder;
import com.apicatalog.trust.signature.SignatureGenerator;

public class AtomicCryptoSuite implements CryptoSuite {

    String id;
    String c14n; // JCS, RDFC, ..
    Multibase multibase;
    SignatureDecoder signatureDecoder;
    SignatureGenerator<DataIntegrityProof> signatureGenerator;

    public AtomicCryptoSuite(
            String id,
            String c14n,
            Multibase multibase,
            SignatureDecoder signatureDecoder,
            SignatureGenerator<DataIntegrityProof> signatureGenerator) {
        this.id = id;
        this.c14n = c14n;
        this.multibase = multibase;

        this.signatureGenerator = signatureGenerator;
        this.signatureDecoder = signatureDecoder;
    }

    @Override
    public Signature decode(String encoded, Proof proof, Data data) {
        return signatureDecoder.decode(encoded, proof, data);
    }

    public DataIntegrityProof generateProof(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof.Draft proofDraft,
            Data data) throws SignatureException {

        proofDraft.canonize(c14n);

        var unsigned = proofDraft.get();

        var signature = signatureGenerator.generate(
                algorithm,
                signer,
                digestFactory,
                unsigned,
                data);

        proofDraft.signature(signature);
        return proofDraft.get();
    }

    @Override
    @Deprecated
    public String encode(Signature signature) {
        return multibase.encode(signature.toByteArray());
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String c14n() {
        return c14n;
    }

}
