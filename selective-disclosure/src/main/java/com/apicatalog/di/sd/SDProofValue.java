package com.apicatalog.di.sd;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Collection;

import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.payload.CanonicalPayload;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.UnicodeString;

public abstract class SDProofValue implements Signature {

    protected String signatureAlgorithm;
    protected String digestAlgorithm;

    protected Proof proof;
    protected SDPayload payload;

    protected byte[] proofPublicKey;
    protected Multicodec proofPublicKeyCodec;

    protected byte[] baseSignature;
    protected Collection<byte[]> signatures;

    @Override
    public boolean verify(
            AsymmetricVerifier verifier,
            Digestor.Factory digestFactory,
            byte[] publicKey)
            throws InvalidKeyException, SignatureException {
        return verify(verifier, verifier, digestFactory, publicKey);
    }

    public boolean verify(
            AsymmetricVerifier baseVerifier,
            AsymmetricVerifier proofVerifier,
            Digestor.Factory digestFactory,
            byte[] publicKey)
            throws InvalidKeyException, SignatureException {

        if (signatures.size() != payload.redactablePayload().size()) {
            throw new SignatureException();
        }

        var digestor = digestFactory.newDigestor(digestAlgorithm);

        var proofDigest = digestor.digest(proof.canonicalPayload());
        var mandatoryDigest = digestor.digest(payload.canonicalPayload());

        var baseDigest = hash(
                proofDigest,
                proofPublicKey,
                mandatoryDigest);

        var isBaseSignatureVerified = baseVerifier.verify(publicKey, baseDigest, baseSignature);

        if (!isBaseSignatureVerified) {
            return false;
        }

        var decodedProofPublicKey = proofPublicKeyCodec.decode(proofPublicKey);

        var redactableIterator = payload.redactablePayload().iterator();

        for (var signature : signatures) {

            var redactable = redactableIterator.next();

            if (!proofVerifier.verify(decodedProofPublicKey, redactable, signature)) {
                return false;
            }
        }

        // all good
        return true;
    }

    @Override
    public Proof proof() {
        return proof;
    }

    @Override
    public String algorithm() {
        return signatureAlgorithm;
    }

    static byte[] byteArray(DataItem item) {
        if (!MajorType.BYTE_STRING.equals(item.getMajorType())) {
//      throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }
        return ((ByteString) item).getBytes();
    }

    static String string(DataItem item) {
        if (!MajorType.UNICODE_STRING.equals(item.getMajorType())) {
//      throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }
        return ((UnicodeString) item).getString();
    }

//    private static int toUInt(DataItem item) {
//        if (!MajorType.UNSIGNED_INTEGER.equals(item.getMajorType())) {
////          throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
////
//        return ((UnsignedInteger) item).getValue().intValueExact();
//    }

    static final byte[] hash(
            final byte[] proofHash,
            final byte[] proofPublicKey,
            byte[] mandatoryHash) {

        final byte[] hash = new byte[proofHash.length
                + proofPublicKey.length
                + mandatoryHash.length];

        System.arraycopy(proofHash, 0, hash, 0, proofHash.length);
        System.arraycopy(proofPublicKey, 0, hash, proofHash.length, proofPublicKey.length);
        System.arraycopy(mandatoryHash, 0, hash, proofHash.length + proofPublicKey.length, mandatoryHash.length);
        return hash;
    }

    @Override
    public CanonicalPayload payload() {
        return payload;
    }

    public byte[] proofPublicKey() {
        return proofPublicKey;
    }
}
