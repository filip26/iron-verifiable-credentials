package com.apicatalog.di.sd;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.sd.SDGraphProcessor.SignatureAlgorithm;
import com.apicatalog.di.sd.signature.DerivedSignature;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.proof.Proof;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.UnsignedInteger;

public final class SDDerivedProofValue implements DerivedSignature {

    protected static final byte[] BYTE_PREFIX = new byte[] { (byte) 0xd9, 0x5d, 0x01 };

    private String signatureAlgorithm;
    private String digestAlgorithm;

    private SDPayload payload;
    private Proof proof;

    private byte[] baseSignature;

    private byte[] proofPublicKey;
    private Multicodec proofPublicKeyCodec;

    private byte[][] signatures;

    private SDDerivedProofValue() {
//        // TODO Auto-generated constructor stub
    }

    public static boolean isAccepted(byte[] signature) {
        return signature.length > 2
                && signature[0] == BYTE_PREFIX[0]
                && signature[1] == BYTE_PREFIX[1]
                && signature[2] == BYTE_PREFIX[2];
    }

    public static SDDerivedProofValue decode(
            byte[] signature,
            Function<Integer, SignatureAlgorithm> algorithmProvider,
            Function<byte[], Multicodec> proofPublicKeyDecoder,
            Proof proof,
            // TODO DerivedPayload?
            SDGraphProcessor processor) {

        Objects.requireNonNull(signature);

        if (signature.length < 3) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }

        final ByteArrayInputStream is = new ByteArrayInputStream(signature);

        if ((byte) is.read() != BYTE_PREFIX[0] || is.read() != BYTE_PREFIX[1] || is.read() != BYTE_PREFIX[2]) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }

        final CborDecoder decoder = new CborDecoder(is);

        try {
            final List<DataItem> cbor = decoder.decode();

            if (cbor.size() != 1) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            if (!MajorType.ARRAY.equals(cbor.get(0).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            final Array top = (Array) cbor.get(0);

            if (top.getDataItems().size() != 5) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            final SDDerivedProofValue proofValue = new SDDerivedProofValue();
            proofValue.proof = proof;
            proofValue.baseSignature = SDBaseProofValue.byteArray(top.getDataItems().get(0));
            proofValue.proofPublicKey = SDBaseProofValue.byteArray(top.getDataItems().get(1));
            proofValue.proofPublicKeyCodec = proofPublicKeyDecoder.apply(proofValue.proofPublicKey);

            final var algorithms = algorithmProvider.apply(proofValue.baseSignature.length);

            proofValue.signatureAlgorithm = algorithms.signature();
            proofValue.digestAlgorithm = algorithms.digest();

            if (!MajorType.ARRAY.equals(top.getDataItems().get(2).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            proofValue.signatures = new byte[((Array) top.getDataItems().get(2)).getDataItems().size()][];

            int signatureIndex = 0;
            for (final var item : ((Array) top.getDataItems().get(2)).getDataItems()) {
                proofValue.signatures[signatureIndex++] = SDBaseProofValue.byteArray(item);
            }

            // label map
            if (!MajorType.MAP.equals(top.getDataItems().get(3).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            final co.nstant.in.cbor.model.Map labels = (co.nstant.in.cbor.model.Map) top.getDataItems().get(3);

            var labelMap = new LinkedHashMap<Integer, byte[]>(labels.getKeys().size());

            for (final var key : labels.getKeys()) {
                labelMap.put(toUInt(key), SDBaseProofValue.byteArray(labels.get(key)));
            }

            // indices
            if (!MajorType.ARRAY.equals(top.getDataItems().get(4).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
            }

            var indices = new int[(((Array) top.getDataItems().get(4)).getDataItems().size())];

            for (int i = 0; i < indices.length; i++) {
                final DataItem item = ((Array) top.getDataItems().get(4)).getDataItems().get(i);
                indices[i] = toUInt(item);
            }

            proofValue.payload = processor.derived(labelMap, indices);

            return proofValue;

        } catch (CborException e) {
//            throw new DocumentError(e, ErrorType.Invalid, "ProofValue");
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean verify(
            AsymmetricVerifier verifier,
            Digestor.Factory digestFactory,
            byte[] publicKey) throws InvalidKeyException, SignatureException {

        if (signatures.length != payload.redactablePayload().length) {
//          throw new VerificationError(VerificationErrorCode.InvalidSignature);
            throw new SignatureException();
        }

//        IO.println(expanded);
//        IO.println(labels);
//        IO.println(Arrays.toString(indices));

        var digestor = digestFactory.newDigestor(digestAlgorithm);
//
        var proofDigest = digestor.digest(proof.canonicalPayload());
        var mandatoryDigest = digestor.digest(payload.canonicalPayload());

        var baseDigest = SDBaseProofValue.hash(
                proofDigest,
                proofPublicKey,
                mandatoryDigest);

        var isBaseSignatureVerified = verifier.verify(publicKey, baseDigest, baseSignature);

        if (!isBaseSignatureVerified) {
            return false;
        }

        var decodedProofPublicKey = proofPublicKeyCodec.decode(proofPublicKey);

        for (int signatureIndex = 0; signatureIndex < signatures.length; signatureIndex++) {

            var redactable = payload.redactablePayload()[signatureIndex];
            var signature = signatures[signatureIndex];

            if (!verifier.verify(decodedProofPublicKey, redactable, signature)) {
                return false;
            }
        }

        // all good
        return true;
    }

    @Override
    public String algorithm() {
        return signatureAlgorithm;
    }

    @Override
    public DigestiblePayload payload() {
        return null; // FIXME
    }

    @Override
    public Proof proof() {
        return proof;
    }

    @Override
    public byte[] toByteArray() {
        // TODO Auto-generated method stub
        return null;
    }

    private static int toUInt(DataItem item) {

        if (!MajorType.UNSIGNED_INTEGER.equals(item.getMajorType())) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }

        return ((UnsignedInteger) item).getValue().intValueExact();
    }

    public static SDDerivedProofValue generate(SDBaseProofValue base, SDDerivedDocument document) {

        var signature = new SDDerivedProofValue();
        signature.baseSignature = base.baseSignature;
        signature.digestAlgorithm = base.digestAlgorithm;
        signature.payload = document;
        signature.proofPublicKey = base.proofPublicKey;
        signature.proofPublicKeyCodec = base.proofPublicKeyCodec;
        signature.signatureAlgorithm = base.signatureAlgorithm;

        signature.signatures = new byte[document.disclosedIndices.length][];

        for (int index = 0; index < signature.signatures.length; index++) {
            signature.signatures[index] = base.signatures[document.disclosedIndices[index]];
        }

        var d = new DataIntegrityProof.Draft(((DataIntegrityProof) base.proof).cryptosuite());
        d.proof(((DataIntegrityProof) base.proof));

        signature.proof = d.signed(signature);

//        signature.proof 
//        signature.p
//        var derivedProof = DataIntegrityProof.clone((DataIntegrityProof) proof, signature);

        // TODO Auto-generated method stub
        return signature;
    }

//    protected ECDSASDDerivedProofValue(
//            Proof proof,
//            DocumentModel model,
//            DocumentLoader loader) {
//        this.model = model;
//        this.proof = proof;
//        this.loader = loader;
//    }
//
//
//    public static byte[] toByteArray(
//            byte[] baseSignature,
//            byte[] proofPublicKey,
//            Collection<byte[]> signatures,
//            Map<Integer, byte[]> labels,
//            int[] indices) {
//
//        final CborBuilder cbor = new CborBuilder();
//
//        final ArrayBuilder<CborBuilder> top = cbor.addArray();
//
//        top.add(baseSignature); // .tagged(64);
//        top.add(proofPublicKey); // .tagged(64);
//
//        final ArrayBuilder<ArrayBuilder<CborBuilder>> cborSigs = top.addArray();
//
//        signatures.forEach(m -> cborSigs.add(m)); // .tagged(64));
//
//        final MapBuilder<ArrayBuilder<CborBuilder>> cborLabels = top.addMap();
//
//        labels.entrySet().forEach(e -> cborLabels.put(e.getKey(), e.getValue())); // .tagged(64));
//
//        final ArrayBuilder<ArrayBuilder<CborBuilder>> cborIndices = top.addArray();
//
//        for (int i = 0; i < indices.length; i++) {
//            cborIndices.add(new UnsignedInteger(indices[i]));
//        }
//
//        try {
//            final ByteArrayOutputStream out = new ByteArrayOutputStream();
//            out.write(BYTE_PREFIX);
//
//            (new CborEncoder(out)).encode(cbor.build());
//
//            return out.toByteArray();
//        } catch (IOException | CborException e) {
//            throw new IllegalArgumentException(e);
//        }
//    }
//
//    @Override
//    public Proof proof() {
//        return proof;
//    }
//
//    protected static byte[] toByteArray(DataItem item) throws DocumentError {
//
//        if (!MajorType.BYTE_STRING.equals(item.getMajorType())) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//
//        return ((ByteString) item).getBytes();
//    }
//

//    @Override
//    public void verify(VerificationKey key) throws VerificationError, DocumentError {
//        final SDProofValue signer = new SDProofValue(proof.cryptosuite(), proof.cryptosuite(), proof.cryptosuite());
//
//        try {
//            final byte[] proofHash = signer.hash(model.proofs().iterator().next());
//
//            // unsignedProof.context()
//            final RecoveredIndices verifyData = RecoveredIndices.of(model.data().expanded(), loader, labels, indices);
//
//            final byte[] mandatoryHash = signer.hash(verifyData.mandatory);
//
//            if (signatures.size() != verifyData.nonMandatory.size()) {
//                throw new VerificationError(VerificationErrorCode.InvalidSignature);
//            }
//
//            final byte[] signature = SDProofValue.hash(proofHash, proofPublicKey, mandatoryHash);
//
//            signer.verify(key.publicKey().rawBytes(), baseSignature, signature);
//
//            final byte[] decodedProofPublicKey = ECDSASD2023.CODECS.decode(proofPublicKey);
//
//            int i = 0;
//            for (byte[] sig : signatures) {
//                signer.verify(decodedProofPublicKey, sig, (verifyData.nonMandatory.get(i).toString() + '\n').getBytes(StandardCharsets.UTF_8));
//                i++;
//            }
//            // all good
//
//        } catch (CryptoSuiteError | DocumentError e) {
//            throw new VerificationError(VerificationErrorCode.InvalidSignature, e);
//        }
//    }
//
//    @Override
//    public String toString() {
//        final StringBuilder string = new StringBuilder();
//        string.append("DerivedProofValue").append('\n')
//                .append("  baseSignature: ").append(baseSignature != null ? Hex.toHexString(baseSignature) : "n/a").append('\n')
//                .append("  publicKey: ").append(proofPublicKey != null ? Multibase.BASE_58_BTC.encode(proofPublicKey) : "n/a").append('\n')
//                .append("  signatures:\n");
//
//        if (signatures != null) {
//            signatures.stream().map(Hex::toHexString).forEach(s -> string
//                    .append("    ")
//                    .append(s)
//                    .append('\n'));
//        }
//
//        string.append("  labels:\n");
//
//        if (labels != null) {
//            labels.entrySet().forEach(e -> string
//                    .append("    ")
//                    .append(e.getKey())
//                    .append(" -> ")
//                    .append(e.getValue() != null ? Multibase.BASE_64_URL.encode(e.getValue()) : "n/a")
//                    .append('\n'));
//        }
//        return string
//                .append("  indicies: ")
//                .append(indices != null ? Arrays.toString(indices) : "n/a")
//                .append('\n')
//                .toString();
//    }
//
//    @Override
//    public byte[] byteArrayValue() {
//        return toByteArray(baseSignature, proofPublicKey, signatures, labels, indices);
//    }
//
//    @Override
//    public DocumentModel model() {
//        return model;
//    }

}
