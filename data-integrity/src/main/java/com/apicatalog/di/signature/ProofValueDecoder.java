package com.apicatalog.di.signature;

import java.security.MessageDigest;

import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureDecoder;

public class ProofValueDecoder implements SignatureDecoder {

    String id;
    String algorithm; // P-256, P-384, Ed25519, ML-DSA-44, ...
    String c14n; // JCS, RDFC, ..
    String digestName;
    int length;

    Multibase multibase;
    MessageDigest digest;

    public ProofValueDecoder(
            String algorithm,
            Multibase multibase,
            MessageDigest digest,
            int length) {
        this.algorithm = algorithm;
        this.multibase = multibase;
        this.digest = digest;
    }

    @Override
    public Signature decode(String value, Proof proof, Data data) {
        return ProofValue.newSignature(
                algorithm,
                digest,
                multibase.decode(value),
                proof,
                data);
    }

//    public DataIntegrityProof generateProof(
//            AsymmetricSigner signer,
//            DataIntegrityProof.Draft proofDraft,
//            Data data) throws SignatureException {
//
//        proofDraft.canonize(c14n);
//
//        var unsigned = proofDraft.get();
//
//        var signature = ProofValue.generateSignature(
//                signer,
//                unsigned.cryptosuite().algorithm(),
//                digestor,
//                unsigned,
//                data);
//
//        proofDraft.signature(signature);
//        return proofDraft.get();
//    }
//    
//
//    @Override
//    public byte[] decode(String value) {
//        return multibase.decode(value);
//    }

}
