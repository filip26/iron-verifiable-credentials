package com.apicatalog.di.sd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.sd.SDGraphProcessor.SignatureAlgorithm;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.BaseSignature;
import com.apicatalog.trust.signature.Signature;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.builder.ArrayBuilder;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.UnicodeString;

public final class SDBaseProofValue implements BaseSignature {

    private static final byte[] BYTE_PREFIX = new byte[] { (byte) 0xd9, 0x5d, 0x00 };

    private String signatureAlgorithm;
    private String digestAlgorithm;

    private Proof proof;
    private RedactablePayload payload;

    private byte[] baseSignature;
    private byte[] proofPublicKey;
    private byte[] hmacKey;

    private Collection<byte[]> signatures;
    private Collection<String> mandatoryPointers;

    private Multicodec proofPublicKeyCodec;

    public static boolean isAccepted(byte[] signature) {
        return signature.length > 2
                && signature[0] == BYTE_PREFIX[0]
                && signature[1] == BYTE_PREFIX[1]
                && signature[2] == BYTE_PREFIX[2];
    }

    public static Signature decode(
            byte[] signature,
            Function<Integer, SignatureAlgorithm> algorithmProvider,
            Function<byte[], Multicodec> proofPublicKeyDecoder,
            Proof proof,
            PayloadProcessor data) {

        Objects.requireNonNull(signature);

        if (signature.length < 3) {
//          throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }

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

            final Array top;

            if (cbor.get(0) instanceof Array array) {
                top = array;

            } else {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
                throw new IllegalArgumentException();
            }

            if (top.getDataItems().size() != 5) {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
                throw new IllegalArgumentException();
            }

            final var proofValue = new SDBaseProofValue();
            proofValue.proof = proof;
            proofValue.baseSignature = byteArray(top.getDataItems().get(0));

            final var algorithms = algorithmProvider.apply(proofValue.baseSignature.length);
            proofValue.signatureAlgorithm = algorithms.signature();
            proofValue.digestAlgorithm = algorithms.digest();

            proofValue.proofPublicKey = byteArray(top.getDataItems().get(1));
            proofValue.proofPublicKeyCodec = proofPublicKeyDecoder.apply(proofValue.proofPublicKey);

            proofValue.hmacKey = byteArray(top.getDataItems().get(2));

            if (top.getDataItems().get(3) instanceof Array signatures) {
                proofValue.signatures = new ArrayList<>(signatures.getDataItems().size());

                for (final var item : signatures.getDataItems()) {
                    proofValue.signatures.add(byteArray(item));
                }

            } else {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
                throw new IllegalArgumentException();
            }

            if (top.getDataItems().get(4) instanceof Array pointers) {

                proofValue.mandatoryPointers = new ArrayList<>(pointers.getDataItems().size());

                for (final var item : pointers.getDataItems()) {
                    proofValue.mandatoryPointers.add(string(item));
                }

            } else {
//              throw new DocumentError(ErrorType.Invalid, "ProofValue");
                throw new IllegalArgumentException();
            }

            proofValue.payload = data.redactable(
                    proofValue.mandatoryPointers,
                    Map.of("HMAC_KEY", proofValue.hmacKey));

            return proofValue;

        } catch (CborException e) {
            throw new IllegalArgumentException(e);
//          throw new DocumentError(e, ErrorType.Invalid, "ProofValue");
        }
    }

    public static SDBaseProofValue generateSignature(
            String signatureAlgorithm,
            String digestAlgorithm,
            AsymmetricSigner baseSigner,
            byte[] proofPublicKey,
            Multicodec proofPublicKeyCodec,
            AsymmetricSigner proofSigner,
            Digestor digestor,
            DataIntegrityProof unsignedProof,
            RedactablePayload payload) throws SignatureException {

        var proofDigest = digestor.digest(unsignedProof.canonicalPayload());
        var dataDigest = digestor.digest(payload.canonicalPayload());

        var digest = hash(
                proofDigest,
                proofPublicKey,
                dataDigest);

        var proofValue = new SDBaseProofValue();
        proofValue.proof = unsignedProof;
        proofValue.signatureAlgorithm = signatureAlgorithm;
        proofValue.digestAlgorithm = digestAlgorithm;

        proofValue.baseSignature = baseSigner.sign(digest);
        proofValue.hmacKey = ((PayloadWithHmac) payload).hmacKey();
        proofValue.proofPublicKey = proofPublicKey;
        proofValue.proofPublicKeyCodec = proofPublicKeyCodec;
        proofValue.mandatoryPointers = payload.pointers();

        if (payload.redactablePayload() != null && !payload.redactablePayload().isEmpty()) {

            proofValue.signatures = new ArrayList<byte[]>(payload.redactablePayload().size());

            for (var optional : payload.redactablePayload()) {
                proofValue.signatures.add(proofSigner.sign(optional.getValue()));
            }
        }

        proofValue.payload = payload;

        return proofValue;
    }

    public static byte[] toByteArray(
            byte[] baseSignature,
            byte[] proofPublicKey,
            byte[] hmacKey,
            Collection<byte[]> signatures,
            Collection<String> pointers) {

        final CborBuilder cbor = new CborBuilder();

        final ArrayBuilder<CborBuilder> top = cbor.addArray();

        top.add(baseSignature);
        top.add(proofPublicKey);
        top.add(hmacKey);

        final ArrayBuilder<ArrayBuilder<CborBuilder>> cborSigs = top.addArray();

        if (signatures != null) {
            signatures.forEach(m -> cborSigs.add(m));
        }

        final ArrayBuilder<ArrayBuilder<CborBuilder>> cborPointers = top.addArray();

        if (pointers != null) {
            pointers.forEach(cborPointers::add);
        }

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(BYTE_PREFIX);

            (new CborEncoder(out)).encode(cbor.build());

            return out.toByteArray();
        } catch (IOException | CborException e) {
            throw new IllegalStateException(e);
        }
    }

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
            return false;
        }

        var digestor = digestFactory.newDigestor(digestAlgorithm);

        var proofDigest = digestor.digest(proof.canonicalPayload());
        var dataDigest = digestor.digest(payload.canonicalPayload());

        var digest = hash(
                proofDigest,
                proofPublicKey,
                dataDigest);

        var isBaseSignatureVerified = baseVerifier.verify(publicKey, digest, baseSignature);

        if (!isBaseSignatureVerified) {
            return false;
        }

        var decodedProofPublicKey = proofPublicKeyCodec.decode(proofPublicKey);

        var redactableIterator = payload.redactablePayload().iterator();

        for (var signature : signatures) {

            var redactable = redactableIterator.next();

            if (!proofVerifier.verify(decodedProofPublicKey, redactable.getValue(), signature)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Proof proof() {
        return proof;
    }

    @Override
    public byte[] toByteArray() {
        return toByteArray(baseSignature, proofPublicKey, hmacKey, signatures, mandatoryPointers);
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

    private static byte[] hash(
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
    public DigestiblePayload payload() {
        return payload;
    }

    @Override
    public Collection<String> mandatoryPointers() {
        return mandatoryPointers;
    }

    public byte[] proofPublicKey() {
        return proofPublicKey;
    }

    public byte[] hmacKey() {
        return hmacKey;
    }
}
