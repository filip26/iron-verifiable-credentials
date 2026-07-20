package com.apicatalog.di.sd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.sd.signature.BaseSignature;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.signature.Signature;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;

public final class SDBaseProofValue extends SDProofValue<SDBaseDocument> implements BaseSignature {

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
            DataIntegrityProof proof,
            SDPayloadGenerator payload) {

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
                proofValue.signatures = new byte[signatures.getDataItems().size()][];

                var index = 0;
                for (final var item : signatures.getDataItems()) {
                    proofValue.signatures[index++] = byteArray(item);
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

            proofValue.payload = payload.redactable(
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
            SDBaseDocument payload) throws SignatureException {

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
        proofValue.mandatoryPointers = payload.mandatoryPointers();

        if (payload.redactablePayload() != null && payload.redactablePayload().length > 0) {

            proofValue.signatures = new byte[payload.redactablePayload().length][];

            for (int index = 0; index < proofValue.signatures.length; index++) {
                proofValue.signatures[index] = proofSigner.sign(payload.redactablePayload()[index]);
            }
        }

        proofValue.payload = payload;

        return proofValue;
    }

    public static byte[] toByteArray(
            byte[] baseSignature,
            byte[] proofPublicKey,
            byte[] hmacKey,
            byte[][] signatures,
            Collection<String> pointers) {

        final CborBuilder cbor = new CborBuilder();

        var top = cbor.addArray();

        top.add(baseSignature);
        top.add(proofPublicKey);
        top.add(hmacKey);

        var cborSignatures = top.addArray();

        for (int signatureIndex = 0; signatureIndex < signatures.length; signatureIndex++) {
            cborSignatures.add(signatures[signatureIndex]);
        }

        var cborPointers = top.addArray();

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

    public SDDerivedProofValue derive(Collection<String> selectors) {

        var combinedPointers = new LinkedHashSet<String>(mandatoryPointers.size() + selectors.size());
        combinedPointers.addAll(mandatoryPointers);
        combinedPointers.addAll(selectors);

        var selection = Pointer.select(payload.compacted, combinedPointers);

        var selectedNQuads = new HashSet<String>();

        var c14n = payload.model.newCanonizer();
        var consumer = c14n.consumer();

        payload.model.tordf().accept(selection, ((subject, predicate, object, datatype, language, direction, graph) -> {

            var s = subject;
            if (s.startsWith(Skolemizer.URN_PREFIX)) {
                s = payload.labels.get("_:" + s.substring(Skolemizer.URN_PREFIX.length()));
            }
            var o = object;
            if (o.startsWith(Skolemizer.URN_PREFIX)) {
                o = payload.labels.get("_:" + o.substring(Skolemizer.URN_PREFIX.length()));
            }

            var nquad = c14n.toNQuad(s, predicate, o, datatype, language, direction, graph);

            consumer.accept(s, predicate, o, datatype, language, direction, graph);
            selectedNQuads.add(nquad);
        }));

        c14n.canonize();

        var selectionLabels = HashMap.<Integer, byte[]>newHashMap(c14n.labels().size());
        for (var label : c14n.labels().entrySet()) {
            selectionLabels.put(
                    Integer.parseInt(label.getValue().substring("_:c14n".length())),
                    Multibase.BASE_64_URL.decode(label.getKey().substring("_:".length())));
        }

        int index = 0;

        var selectedIndices = new int[selectedNQuads.size()];
        int selectedIndex = 0;

        for (var nquad : payload.canonized) { // TODO iterate over selected?!
            if (selectedNQuads.contains(nquad)) {
                selectedNQuads.remove(nquad);

                selectedIndices[selectedIndex++] = index;

                // all selected indices found
                if (selectedNQuads.isEmpty()) {
                    break;
                }
            }
            index++;
        }

        var indices = relativeIndices(selectedIndices, payload.mandatoryIndices);

        var disclosedPayload = new byte[selectedIndices.length - payload.mandatoryIndices.length][];
        var signatureIndex = 0;

        var selectedSignatures = new byte[selectedIndices.length - payload.mandatoryIndices.length][];

        for (var si : selectedIndices) {
            var ri = Arrays.binarySearch(payload.redactableIndices, si);
            if (ri >= 0) {
                disclosedPayload[signatureIndex] = payload.redactable[ri];
                selectedSignatures[signatureIndex++] = signatures[ri];
            }
        }

        if (signatureIndex < selectedSignatures.length) {
            throw new IllegalStateException();
        }

        var derivedDocument = new SDDerivedDocument(
                () -> SDBaseProofValue.recompact(payload.context, selection, payload.model),
                payload.base,
                disclosedPayload,
                indices,
                selectionLabels);

        return SDDerivedProofValue.newInstance(this, derivedDocument, selectedSignatures);
    }

    private static Map<String, Object> recompact(
            Collection<String> context,
            Map<String, Object> document,
            SemanticModel model) {

        var expanded = model.expand().apply(document);

        var deskolemized = Skolemizer.deskolemizeExpanded(expanded);

        return model.compact().apply(context, (Map<String, Object>) deskolemized.iterator().next());
    }

    private static int[] relativeIndices(int[] combined, int[] mandatory) {
        final var indices = new int[mandatory.length];

        int index = 0;
        int relative = 0;

        Arrays.sort(mandatory); // TODO all indices should be sorted already

        for (int key : combined) {
            if (Arrays.binarySearch(mandatory, key) >= 0) {
                indices[index++] = relative;
            }
            relative++;
        }
        return indices;
    }

    @Override
    public Collection<String> mandatoryPointers() {
        return mandatoryPointers;
    }

    public byte[] hmacKey() {
        return hmacKey;
    }
}
