package com.apicatalog.di.std;

import java.security.SignatureException;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.Digestor;
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

    public ProofDraft createProofDraft() throws SignatureException {
        return new ProofDraft(this);
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

    public static class ProofDraft extends DataIntegrityProof.Draft {

        final SignatureGenerator<DataIntegrityProof> signatureGenerator;

        public ProofDraft(StandardCryptoSuite cryptosuite) {
            super(cryptosuite);
            this.signatureGenerator = cryptosuite.signatureGenerator;
        }

        public DataIntegrityProof sign(
                String algorithm,
                AsymmetricSigner signer,
                Digestor.Factory digestFactory,
                DigestiblePayload payload) throws SignatureException {

            canonize(proof.cryptosuite().c14n());

            var unsigned = unsigned();

            var signature = signatureGenerator.generate(
                    algorithm,
                    signer,
                    digestFactory,
                    unsigned,
                    payload);

            return signed(signature);
        }
    }
}
