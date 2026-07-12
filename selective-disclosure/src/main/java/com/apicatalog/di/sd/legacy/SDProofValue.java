package com.apicatalog.di.sd.legacy;

public class SDProofValue {

//    protected final SignatureAlgorithm signer;
//    protected final CanonicalizationMethod canonicalizer;
//    protected final DigestAlgorithm digest;
//
//    public SDProofValue(final SignatureAlgorithm signer, final CanonicalizationMethod canonicalizer, DigestAlgorithm digest) {
//        this.signer = signer;
//        this.canonicalizer = canonicalizer;
//        this.digest = digest;
//    }

//    public byte[] signature(
//            VerifiableMaterial unsignedProof,
//            Collection<RdfNQuad> mandatory,
//            byte[] proofPublicKey,
//            byte[] privateKey) throws CryptoSuiteError {
//
//        return signer.sign(privateKey, hash(
//                hash(unsignedProof),
//                proofPublicKey,
//                hash(mandatory)));
//    }
//
//    public static byte[] hash(
//            final byte[] proofHash,
//            final byte[] proofPublicKey,
//            byte[] mandatoryHash) {
//
//        final byte[] hash = new byte[proofHash.length
//                + proofPublicKey.length
//                + mandatoryHash.length];
//
//        System.arraycopy(proofHash, 0, hash, 0, proofHash.length);
//        System.arraycopy(proofPublicKey, 0, hash, proofHash.length, proofPublicKey.length);
//        System.arraycopy(mandatoryHash, 0, hash, proofHash.length + proofPublicKey.length, mandatoryHash.length);
//        return hash;
//    }
//
//    public byte[] hash(VerifiableMaterial unsignedProof) throws CryptoSuiteError {
//        return digest.digest(canonicalizer.canonicalize(unsignedProof));
//    }
//
//    public byte[] hash(final Collection<RdfNQuad> nquads) throws CryptoSuiteError {
//        StringWriter writer = new StringWriter(nquads.size() * 100);
//
//        nquads.stream().forEach(x -> writer.write(x.toString() + '\n'));
//
//        return digest.digest(writer.toString().getBytes(StandardCharsets.UTF_8));
//    }
//
//    public Collection<byte[]> signatures(final Collection<RdfNQuad> nquads, byte[] proofPrivateKey) throws CryptoSuiteError {
//        final Collection<byte[]> signatures = new ArrayList<>(nquads.size());
//        for (final RdfNQuad nquad : nquads) {
//            signatures.add(signature(nquad, proofPrivateKey));
//        }
//        return signatures;
//    }
//
//    public byte[] signature(final RdfNQuad nquad, byte[] proofPrivateKey) throws CryptoSuiteError {
//        return signer.sign(proofPrivateKey, (nquad.toString() + '\n').getBytes(StandardCharsets.UTF_8));
//    }
//
//    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
//        signer.verify(publicKey, signature, data);
//    }
}
