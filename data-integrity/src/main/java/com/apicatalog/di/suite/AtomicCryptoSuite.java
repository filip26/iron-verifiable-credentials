package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.Signature;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;

public class AtomicCryptoSuite implements CryptoSuite {

    String id;
    String algorithm; // P-256, P-384, Ed25519, ML-DSA-44, ...
    String c14n; // JCS, RDFC, ..
    String digestName;

    int signatureLength;
    Multibase multibase;

    public AtomicCryptoSuite(
            String id,
            String algorithm,
            String c14n,
            String digestName,
            Multibase multibase,
            int signatureLength) {
        this.id = id;
        this.algorithm = algorithm;
        this.c14n = c14n;
        this.digestName = digestName;
        this.multibase = multibase;
        this.signatureLength = signatureLength;
    }

    public DataIntegrityProof generateProof(
            AsymmetricSigner signer,
            DataIntegrityProof.Draft proofDraft,
            Data data) throws SignatureException {

        try {
            proofDraft.canonize(c14n);

            var unsigned = proofDraft.get();

            var signature = ProofValue.generateSignature(
                    signer,
                    unsigned.cryptosuite().algorithm(),
                    MessageDigest.getInstance(digestName),
                    unsigned,
                    data);

            proofDraft.signature(signature);
            return proofDraft.get();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * }
     * 
     * public Signature signDocumentHash(AsymmetricSigner signer, DataIntegrityProof
     * proof, CanonicalDocument canonicalDocument, byte[] documentHash) {
     * 
     */
//  public Map<String, String> sign(Map<String, Object> document, String method) throws SignatureException {
//
//      try {
//          var canonicalDocument = Jcs.canonize(document, JavaAdapter.instance())
//                  .getBytes(StandardCharsets.UTF_8);
//
//          var created = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
//          var nonce = generateNonce(32);
//
//          var canonicalProof = Templates.jcsProof(name, created, method, nonce);
//
//          var hash = hash(digestName, canonicalDocument, canonicalProof);
//
//          var signature = signer.sign(hash);
//
//          return Templates.jsonProof(
//                  name,
//                  created,
//                  method,
//                  nonce,
//                  signatureEncoder.apply(signature));
//
//      } catch (NoSuchAlgorithmException e) {
//          throw new IllegalStateException(e);
//
//      } catch (TreeIOException e) {
//          throw new IllegalArgumentException(e);
//      }
//  }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String algorithm() {
        return algorithm;
    }

    @Override
    public String c14n() {
        return c14n;
    }

    @Override
    public String digest() {
        return digestName;
    }

    @Override
    public String encode(Signature signature) {
        return multibase.encode(signature.toByteArray());
    }

    @Override
    public byte[] decode(String value) {
        return multibase.decode(value);
    }

    @Override
    public Signature newSignature(String value, Proof proof, Data data) {
        try {
            return ProofValue.newSignature(
                    algorithm,
                    MessageDigest.getInstance(digestName),
                    decode(value),
                    proof,
                    data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean isSignature(String value) {
        return multibase.isEncoded(value) && signatureLength == multibase.decode(value).length;
    }
}
