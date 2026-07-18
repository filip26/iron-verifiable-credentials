package com.apicatalog.di.barcodes;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Collection;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.std.StandardCryptoSuite;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.model.ProcessingModel;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class ECDSAXI2023 extends StandardCryptoSuite {

    public static final String P256 = "P-256";
    public static final String P384 = "P-384";

    private static final ECDSAXI2023 ECDSA_XI_2023 = new ECDSAXI2023(
            "ecdsa-xi-2023",
            ProcessingModel.C14N_RDFC);

    private ECDSAXI2023(String id, String c14n) {
        super(id, c14n, Multibase.BASE_58_BTC, ECDSAXI2023::generate);
    }

    public static ECDSAXI2023 getInstance() {
        return ECDSA_XI_2023;
    }

    @Override
    public ProofValue decode(byte[] signature, Proof proof, PayloadGenerator payload) {

        String algorithm = null;
        String digest = null;

        switch (signature.length) {
        case 64:
            algorithm = P256;
            digest = "SHA-256";
            break;
        case 96:
            algorithm = P384;
            digest = "SHA-384";
            break;
        default:
            throw new IllegalArgumentException();
        }

        return ProofValue.newInstance(
                algorithm,
                digest,
                signature,
                (DataIntegrityProof) proof,
                payload);
    }

    private static Signature generate(
            String signatureAlgorithm,
            AsymmetricSigner signer,
            Digestor.Factory digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload payload)
            throws SignatureException {

        if (!(payload instanceof BarcodePayload barcode)) {
            throw new IllegalArgumentException();
        }
        var digestAlgorithm = switch (signatureAlgorithm) {
        case P256 -> Digestor.SHA_256;
        case P384 -> Digestor.SHA_384;
        default -> throw new IllegalArgumentException();
        };

        var digestor = digestFactory.newDigestor(digestAlgorithm);

        return ProofValue.generateSignature(
                signatureAlgorithm,
                digestAlgorithm,
                signer,
                digestor,
                proof,
                barcode);
    }

    public static class ProofValue implements Signature {

        private final String algorithm;
        private final String digestAlgorithm;

        private final byte[] signature;

        private final BarcodePayload payload;
        private final DataIntegrityProof proof;

        private ProofValue(
                String algorithm,
                String digestAlgorithm,
                byte[] signature,
                DataIntegrityProof proof,
                BarcodePayload payload) {
            this.algorithm = algorithm;
            this.digestAlgorithm = digestAlgorithm;
            this.signature = signature;
            this.payload = payload;
            this.proof = proof;
        }

        public static ProofValue newInstance(
                String algorithm,
                String digestAlgorithm,
                byte[] value,
                DataIntegrityProof proof,
                PayloadGenerator payload) {
            return new ProofValue(
                    algorithm,
                    digestAlgorithm,
                    value,
                    proof,
                    payload.digestible(BarcodePayload::new));
        }

        public static ProofValue generateSignature(
                String signatureAlgorithm,
                String digestAlgorithm,
                AsymmetricSigner signer,
                Digestor digestor,
                DataIntegrityProof proof,
                BarcodePayload payload) throws SignatureException {

            var digest = digest(digestAlgorithm, digestor, proof.canonicalPayload(), payload);

            return new ProofValue(
                    signatureAlgorithm,
                    digestAlgorithm,
                    signer.sign(digest),
                    proof,
                    payload);
        }

        @Override
        public boolean verify(
                AsymmetricVerifier verifier,
                Digestor.Factory digestFactory,
                byte[] publicKey)
                throws InvalidKeyException, SignatureException {

            var digestor = digestFactory.newDigestor(digestAlgorithm);

            var digest = digest(digestAlgorithm, digestor, proof.canonicalPayload(), payload);

            return verifier.verify(publicKey, digest, toByteArray());
        }

        /**
         * Computes the cryptographic hash of the canonical proof concatenated with the
         * cryptographic hash of the canonical document.
         * <p>
         * The output is structured as H(canonicalProof) || H(canonicalDocument) and is
         * utilized as signing or verification data.
         * 
         * @param digestAlgorithm
         * @param digest
         * @param canonicalProof    the byte array representing the canonicalized proof
         * @param canonicalDocument the byte array representing the canonicalized
         *                          document
         * @return a byte array containing the concatenated hashes in the specified
         *         order
         */
        private static byte[] digest(
                String digestAlgorithm,
                Digestor digest,
                byte[] canonicalProof,
                BarcodePayload payload) {

            var proofHash = digest.digest(canonicalProof);

            var payloadHash = payload.digest(digestAlgorithm);

            if (payloadHash == null) {
                payloadHash = digest.digest(payload.canonicalPayload());
                payload.digest(digestAlgorithm, payloadHash);
            }

            var dataHash = digest.digest(payload.opticalData());

            return digestFromHash(proofHash, payloadHash, dataHash);
        }

        private static byte[] digestFromHash(byte[] proofHash, byte[] payloadHash, byte[] dataHash) {
            var digest = new byte[proofHash.length + payloadHash.length + dataHash.length];
            System.arraycopy(proofHash, 0, digest, 0, proofHash.length);
            System.arraycopy(payloadHash, 0, digest, proofHash.length, payloadHash.length);
            System.arraycopy(dataHash, 0, digest, proofHash.length + payloadHash.length, dataHash.length);
            return digest;
        }

        @Override
        public byte[] toByteArray() {
            return signature;
        }

        @Override
        public DataIntegrityProof proof() {
            return proof;
        }

        @Override
        public String algorithm() {
            return algorithm;
        }

        @Override
        public BarcodePayload payload() {
            return payload;
        }
    }

    public static class BarcodePayload implements DigestiblePayload {

        byte[] canonicalPayload;
        byte[] opticalData;

        public BarcodePayload(byte[] canonicalPayload) {
            this.canonicalPayload = canonicalPayload;
        }

        @Override
        public byte[] canonicalPayload() {
            return canonicalPayload;
        }

        @Override
        public String c14n() {
            return ProcessingModel.C14N_RDFC;
        }

        @Override
        public void digest(String algorithm, byte[] value) {
            // TODO Auto-generated method stub

        }

        @Override
        public byte[] digest(String algorithm) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<String> digestAlgorithms() {
            // TODO Auto-generated method stub
            return null;
        }

        public byte[] opticalData() {
            return opticalData;
        }

        public void opticalData(byte[] opticalData) {
            this.opticalData = opticalData;
        }

    }
}
