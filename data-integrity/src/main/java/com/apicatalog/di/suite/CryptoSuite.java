package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.processor.PayloadSelector;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureDecoder;
import com.apicatalog.trust.signature.SignatureGenerator;

public class CryptoSuite {

    String id;
    String c14n; // JCS, RDFC, ..
    Multibase multibase;
    SignatureDecoder signatureDecoder;
    SignatureGenerator<DataIntegrityProof> signatureGenerator;

    public CryptoSuite(
            String id,
            String c14n,
            Multibase multibase,    //FIXME replace with signature encoder 
            SignatureDecoder signatureDecoder,
            SignatureGenerator<DataIntegrityProof> signatureGenerator) {
        this.id = id;
        this.c14n = c14n;
        this.multibase = multibase;

        this.signatureGenerator = signatureGenerator;
        this.signatureDecoder = signatureDecoder;
    }

    public Signature decode(String encoded, Proof proof, PayloadSelector payload) {
        return signatureDecoder.decode(encoded, proof, payload);
    }

    public DataIntegrityProof generateProof(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof.Draft proofDraft,
            DigestiblePayload payload) throws SignatureException {

        proofDraft.canonize(c14n);

        var unsigned = proofDraft.get();

        var signature = signatureGenerator.generate(
                algorithm,
                signer,
                digestFactory,
                unsigned,
                payload);

        proofDraft.signature(signature);
        return proofDraft.get();
    }

    @Deprecated
    public String encode(Signature signature) {
        return multibase.encode(signature.toByteArray());
    }

    public String id() {
        return id;
    }

    public String c14n() {
        return c14n;
    }
    
}
