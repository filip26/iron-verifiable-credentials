package com.apicatalog.di.sd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.sd.SDGraphProcessor.BaseDocument;
import com.apicatalog.di.sd.SDGraphProcessor.SignatureAlgorithm;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.BaseSignature;
import com.apicatalog.trust.signature.Signature;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.builder.ArrayBuilder;
import co.nstant.in.cbor.model.Array;

public final class SDBaseProofValue extends SDProofValue implements BaseSignature {

    private static final byte[] BYTE_PREFIX = new byte[] { (byte) 0xd9, 0x5d, 0x00 };

    private byte[] hmacKey;

    private Collection<String> mandatoryPointers;

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
            SDGraphProcessor processor) {

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

            proofValue.payload = processor.redactable(
                    proofValue.mandatoryPointers,
                    proofValue.hmacKey);

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
            BaseDocument payload) throws SignatureException {

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
        proofValue.hmacKey = payload.hmacKey();
        proofValue.proofPublicKey = proofPublicKey;
        proofValue.proofPublicKeyCodec = proofPublicKeyCodec;
        proofValue.mandatoryPointers = payload.pointers();

        if (payload.redactablePayload() != null && !payload.redactablePayload().isEmpty()) {

            proofValue.signatures = new ArrayList<byte[]>(payload.redactablePayload().size());

            for (var optional : payload.redactablePayload()) {
                proofValue.signatures.add(proofSigner.sign(optional));
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
    public byte[] toByteArray() {
        return toByteArray(baseSignature, proofPublicKey, hmacKey, signatures, mandatoryPointers);
    }

    @Override
    public Collection<String> mandatoryPointers() {
        return mandatoryPointers;
    }

    public byte[] hmacKey() {
        return hmacKey;
    }
}
