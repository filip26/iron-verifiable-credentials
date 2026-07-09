package com.apicatalog.di.sd;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.data.DigestiblePayload;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.AtomicSignature;
import com.apicatalog.trust.signature.Signature;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.UnicodeString;

public class BaseProofValue implements AtomicSignature {

    static final byte[] BYTE_PREFIX = new byte[] { (byte) 0xd9, 0x5d, 0x00 };

    private String signatureAlgorithm;
    private String digestAlgorithm;

    private Proof proof;
    private Data data;

    private byte[] baseSignature;
    private byte[] proofPublicKey;
    private byte[] hmacKey;

    private Collection<byte[]> signatures;
    private ArrayList<String> pointers;

    public static boolean isAccepted(byte[] signature) {
        return signature.length > 2
                && signature[0] == BYTE_PREFIX[0]
                && signature[1] == BYTE_PREFIX[1]
                && signature[2] == BYTE_PREFIX[2];
    }

    public static Signature decode(
            String signatureAlgorithm,
            String digestAlgorithm,
            byte[] signature,
            Proof proof,
            Data data) {

//  public static ECDSASDBaseProofValue of(Proof proof, DocumentModel model, byte[] signature, DocumentLoader loader) throws DocumentError {
//
        Objects.requireNonNull(signature);

        // TODO validate signature size
//      if (signature.length < 3) {
//          throw new DocumentError(ErrorType.Invalid, "ProofValue");
//      }
//
        var is = new ByteArrayInputStream(signature);

        if ((byte) is.read() != BYTE_PREFIX[0] || is.read() != BYTE_PREFIX[1] || is.read() != BYTE_PREFIX[2]) {
            throw new IllegalArgumentException();
//          throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }

        final CborDecoder decoder = new CborDecoder(is);

        try {
            final var cbor = decoder.decode();

            if (cbor.size() != 1) {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            if (!MajorType.ARRAY.equals(cbor.get(0).getMajorType())) {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            final var top = (Array) cbor.get(0);

            if (top.getDataItems().size() != 5) {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            final var proofValue = new BaseProofValue();
            proofValue.signatureAlgorithm = signatureAlgorithm;
            proofValue.digestAlgorithm = digestAlgorithm;

            proofValue.data = data;
            proofValue.proof = proof;

            proofValue.baseSignature = byteArray(top.getDataItems().get(0));
            proofValue.proofPublicKey = byteArray(top.getDataItems().get(1));
            proofValue.hmacKey = byteArray(top.getDataItems().get(2));

            if (!MajorType.ARRAY.equals(top.getDataItems().get(3).getMajorType())) {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            proofValue.signatures = new ArrayList<>(((Array) top.getDataItems().get(3)).getDataItems().size());

            for (final DataItem item : ((Array) top.getDataItems().get(3)).getDataItems()) {
                proofValue.signatures.add(byteArray(item));
            }

            if (!MajorType.ARRAY.equals(top.getDataItems().get(4).getMajorType())) {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }
            IO.println("> signatures: " + proofValue.signatures.size());
            proofValue.pointers = new ArrayList<>(((Array) top.getDataItems().get(4)).getDataItems().size());
            IO.println("> mandatory pointers: " + proofValue.pointers);
            for (final DataItem item : ((Array) top.getDataItems().get(4)).getDataItems()) {
                proofValue.pointers.add(string(item));
            }

            return proofValue;

        } catch (CborException e) {
            throw new IllegalArgumentException(e);
//          throw new DocumentError(e, ErrorType.Invalid, "ProofValue");
        }
    }

    @Override
    public Data data() {
        return data;
    }

    @Override
    public Proof proof() {
        return proof;
    }

    @Override
    public byte[] toByteArray() {
        return baseSignature;
    }

    @Override
    public String algorithm() {
        return signatureAlgorithm;
    }

    private static byte[] byteArray(DataItem item) {
        if (!MajorType.BYTE_STRING.equals(item.getMajorType())) {
//      throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }
        return ((ByteString) item).getBytes();
    }

    private static String string(DataItem item) {
        if (!MajorType.UNICODE_STRING.equals(item.getMajorType())) {
//      throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }
        return ((UnicodeString) item).getString();
    }

    @Override
    public boolean verify(
            AsymmetricVerifier verifier,
            Function<String, MessageDigest> digestFactory,
            byte[] publicKey)
            throws InvalidKeyException, SignatureException {

        var digestor = digestFactory.apply(digestAlgorithm);

        var proofDigest = digestor.digest(proof.canonicalPayload());
System.out.println(new String(proof.canonicalPayload()));
        var digest = hash(
                proofDigest,
                proofPublicKey,
                digestor.digest(data.digestiblePayload(Set.of()).canonicalPayload())); // FIXME pass mandatory pointers
IO.println("> digest:" + digest.length);
//        var digest = digest(digestor, proof.canonicalPayload(), data.digestiblePayload(proof.previous()));

        return verifier.verify(publicKey, digest, toByteArray());
    }

    public static byte[] hash(
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

}
