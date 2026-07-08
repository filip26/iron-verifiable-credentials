package com.apicatalog.di.suite;

import java.security.SignatureException;

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
//    String algorithm; // P-256, P-384, Ed25519, ML-DSA-44, ...
    String c14n; // JCS, RDFC, ..
//    String digestName;

    int signatureLength;

    Multibase multibase;
//    MessageDigest digestor;

    SignatureDecoder signatureDecoder;
    SignatureGenerator<DataIntegrityProof> signatureGenerator;
//    Selector selector;

    public AtomicCryptoSuite(
            String id,
//            String algorithm,
            String c14n,
//            String digestName,
            Multibase multibase,
            SignatureDecoder signatureDecoder,
            SignatureGenerator<DataIntegrityProof> signatureGenerator,
            int signatureLength) {
        this.id = id;
//        this.algorithm = algorithm;
        this.c14n = c14n;
//        this.digestName = digestName;
        this.multibase = multibase;

        this.signatureGenerator = signatureGenerator;
        this.signatureDecoder = signatureDecoder;
        this.signatureLength = signatureLength;
    }

    @Override
    public Signature decode(String encoded, Proof proof, Data data) {
        return signatureDecoder.decode(encoded, proof, data);
    }

    public DataIntegrityProof generateProof(
            String algorithm,
            AsymmetricSigner signer,
            DataIntegrityProof.Draft proofDraft,
            Data data) throws SignatureException {

        proofDraft.canonize(c14n);

        var unsigned = proofDraft.get();

        var signature = signatureGenerator.generate(
                algorithm,
                signer,
                unsigned,
                data);

//        var signature = ProofValue.generateSignature(
//                signer,
//                algorithm,
//                digestorFactory,
//                unsigned,
//                data);
//
        proofDraft.signature(signature);
        return proofDraft.get();
    }

//    //TODO deprecate?
//    @Override
//    public Signature newSignature(String value, Proof proof, Data data) {
//        return ProofValue.newSignature(
//                algorithm,
//                digestor,
//                decode(value),
//                proof,
//                data);
//    }

//    @Override
//    public boolean isSignature(String value) {
//        return multibase.isEncoded(value) && 
//                (value.length() >= 65 && value.length() <= 89);
    //// signatureLength == multibase.decode(value).length;
//    }

    @Override
    @Deprecated
    public String encode(Signature signature) {
        return multibase.encode(signature.toByteArray());
    }

//    @Override
//    public byte[] decode(String value) {
//        return multibase.decode(value);
//    }

    @Override
    public String id() {
        return id;
    }

//    @Override
//    public String algorithm() {
//        return algorithm;
//    }

    @Override
    public String c14n() {
        return c14n;
    }

//    @Override
//    public String digest(String keyAlgorithm) {
//        // TODO Auto-generated method stub
//        return null;
//    }

//    @Override
//    public String digest() {
//        return digestName;
//    }

}
