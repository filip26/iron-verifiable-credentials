package com.apicatalog.di.sd.legacy;

public class ECDSASDDerivedProofValue
//implements DerivedProofValue, ByteArrayValue
{

    protected static final byte[] BYTE_PREFIX = new byte[] { (byte) 0xd9, 0x5d, 0x01 };

//    protected final DocumentLoader loader;
//    protected final DocumentModel model;
//    protected final Proof proof;
//
//    protected byte[] baseSignature;
//    protected byte[] proofPublicKey;
//
//    protected Collection<byte[]> signatures;
//    protected Map<Integer, byte[]> labels;
//    protected int[] indices;
//
//    protected ECDSASDDerivedProofValue(
//            Proof proof,
//            DocumentModel model,
//            DocumentLoader loader) {
//        this.model = model;
//        this.proof = proof;
//        this.loader = loader;
//    }
//
//    public static boolean is(byte[] signature) {
//        return signature.length > 2
//                && signature[0] == BYTE_PREFIX[0]
//                && signature[1] == BYTE_PREFIX[1]
//                && signature[2] == BYTE_PREFIX[2];
//    }
//
//    public static ECDSASDDerivedProofValue of(
//            Proof proof,
//            DocumentModel model,
//            byte[] signature,
//            DocumentLoader loader) throws DocumentError {
//
//        Objects.requireNonNull(signature);
//
//        if (signature.length < 3) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//
//        final ByteArrayInputStream is = new ByteArrayInputStream(signature);
//
//        if ((byte) is.read() != BYTE_PREFIX[0] || is.read() != BYTE_PREFIX[1] || is.read() != BYTE_PREFIX[2]) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//
//        final CborDecoder decoder = new CborDecoder(is);
//
//        try {
//            final List<DataItem> cbor = decoder.decode();
//
//            if (cbor.size() != 1) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//
//            if (!MajorType.ARRAY.equals(cbor.get(0).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//
//            final Array top = (Array) cbor.get(0);
//
//            if (top.getDataItems().size() != 5) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//
//            final ECDSASDDerivedProofValue proofValue = new ECDSASDDerivedProofValue(proof, model, loader);
//
//            proofValue.baseSignature = toByteArray(top.getDataItems().get(0));
//            proofValue.proofPublicKey = toByteArray(top.getDataItems().get(1));
//
//            if (!MajorType.ARRAY.equals(top.getDataItems().get(2).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//
//            proofValue.signatures = new ArrayList<>(((Array) top.getDataItems().get(2)).getDataItems().size());
//
//            for (final DataItem item : ((Array) top.getDataItems().get(2)).getDataItems()) {
//                proofValue.signatures.add(toByteArray(item));
//            }
//
//            // label map
//            if (!MajorType.MAP.equals(top.getDataItems().get(3).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//
//            final co.nstant.in.cbor.model.Map labels = (co.nstant.in.cbor.model.Map) top.getDataItems().get(3);
//
//            proofValue.labels = new LinkedHashMap<>(labels.getKeys().size());
//
//            for (final DataItem key : labels.getKeys()) {
//                proofValue.labels.put(toUInt(key), toByteArray(labels.get(key)));
//            }
//
//            // indices
//            if (!MajorType.ARRAY.equals(top.getDataItems().get(4).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//
//            proofValue.indices = new int[(((Array) top.getDataItems().get(4)).getDataItems().size())];
//
//            for (int i = 0; i < proofValue.indices.length; i++) {
//                final DataItem item = ((Array) top.getDataItems().get(4)).getDataItems().get(i);
//                proofValue.indices[i] = toUInt(item);
//            }
//
//            return proofValue;
//
//        } catch (CborException e) {
//            throw new DocumentError(e, ErrorType.Invalid, "ProofValue");
//        }
//    }
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
//    protected static int toUInt(DataItem item) throws DocumentError {
//
//        if (!MajorType.UNSIGNED_INTEGER.equals(item.getMajorType())) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//
//        return ((UnsignedInteger) item).getValue().intValueExact();
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
