package com.apicatalog.di.sd.legacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ECDSASDBaseProofValue
//implements BaseProofValue

{
//
//    static final byte[] BYTE_PREFIX = new byte[] { (byte) 0xd9, 0x5d, 0x00 };
//
//    protected final DocumentLoader loader;
//    protected final Proof proof;
//
//    protected final DocumentModel model;
//
//    protected byte[] baseSignature; 
//    protected byte[] proofPublicKey;
//    protected byte[] hmacKey;
//
//    protected Collection<byte[]> signatures;
//    protected Collection<String> pointers;
//
//    protected ECDSASDBaseProofValue(Proof proof,
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
//    public static ECDSASDBaseProofValue of(Proof proof, DocumentModel model, byte[] signature, DocumentLoader loader) throws DocumentError {
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
//            final ECDSASDBaseProofValue proofValue = new ECDSASDBaseProofValue(proof, model, loader);
//
//            proofValue.baseSignature = byteArray(top.getDataItems().get(0));
//            proofValue.proofPublicKey = byteArray(top.getDataItems().get(1));
//            proofValue.hmacKey = byteArray(top.getDataItems().get(2));
//
//            if (!MajorType.ARRAY.equals(top.getDataItems().get(3).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//
//            proofValue.signatures = new ArrayList<>(((Array) top.getDataItems().get(3)).getDataItems().size());
//
//            for (final DataItem item : ((Array) top.getDataItems().get(3)).getDataItems()) {
//                proofValue.signatures.add(byteArray(item));
//            }
//
//            if (!MajorType.ARRAY.equals(top.getDataItems().get(4).getMajorType())) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//
//            proofValue.pointers = new ArrayList<>(((Array) top.getDataItems().get(4)).getDataItems().size());
//
//            for (final DataItem item : ((Array) top.getDataItems().get(4)).getDataItems()) {
//                proofValue.pointers.add(string(item));
//            }
//
//            return proofValue;
//
//        } catch (CborException e) {
//            throw new DocumentError(e, ErrorType.Invalid, "ProofValue");
//        }
//    }
//
//    public byte[] toByteArray() throws DocumentError {
//        return toByteArray(baseSignature, proofPublicKey, hmacKey, signatures, pointers);
//    }
//
//    public static byte[] toByteArray(
//            byte[] baseSignature,
//            byte[] proofPublicKey,
//            byte[] hmacKey,
//            Collection<byte[]> mandatory,
//            Collection<String> pointers) throws DocumentError {
//
//        final CborBuilder cbor = new CborBuilder();
//
//        final ArrayBuilder<CborBuilder> top = cbor.addArray();
//
//        top.add(baseSignature);
//        top.add(proofPublicKey);
//        top.add(hmacKey);
//
//        final ArrayBuilder<ArrayBuilder<CborBuilder>> cborSigs = top.addArray();
//
//        if (mandatory != null) {
//            mandatory.forEach(m -> cborSigs.add(m));
//        }
//
//        final ArrayBuilder<ArrayBuilder<CborBuilder>> cborPointers = top.addArray();
//
//        if (pointers != null) {
//            pointers.forEach(cborPointers::add);
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
//            throw new DocumentError(e, ErrorType.Invalid, "ProofValue");
//        }
//    }
//
//    @Override
//    public DerivedProofValue derive(final Collection<String> selectors) throws DocumentError {
//
//        if ((selectors == null || selectors.isEmpty()) && (pointers == null || pointers.isEmpty())) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//
//        final ECDSASDDerivedProofValue derived = new ECDSASDDerivedProofValue(proof, model, loader);
//        derived.baseSignature = baseSignature;
//        derived.proofPublicKey = proofPublicKey;
//
//        final HmacIdProvider hmac = HmacIdProvider.newInstance(
//                hmacKey,
//                proof.cryptosuite().keyLength() == 384
//                        ? CurveType.P384
//                        : CurveType.P256);
//
//        final Collection<String> combinedPointers = selectors != null
//                ? Stream.of(pointers, selectors).flatMap(Collection::stream).collect(Collectors.toList())
//                : pointers;
//
//        // FIXME
//        final BaseDocument cdoc = BaseDocument.of(model.data(), loader, hmac);
//
//        Selection mandatory = Selection.of(cdoc, DocumentSelector.of(pointers));
//        Selection combined = Selection.of(cdoc, DocumentSelector.of(combinedPointers));
//
//        derived.indices = mandatory(combined.matching.keySet(), mandatory.matching.keySet());
//
//        Selection selective = Selection.of(cdoc, DocumentSelector.of(selectors));
//
//        derived.signatures = signatures(signatures, mandatory.matching.keySet(), selective.matching.keySet());
//
//        derived.labels = mapping(combined.deskolemizedNQuads, cdoc.labelMap);
//
//        return derived;
//    }
//
//    @Override
//    public Collection<String> pointers() {
//        return pointers;
//    }
//
//    @Override
//    public Proof proof() {
//        return proof;
//    }
//
//    protected static byte[] byteArray(DataItem item) throws DocumentError {
//        if (!MajorType.BYTE_STRING.equals(item.getMajorType())) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//        return ((ByteString) item).getBytes();
//    }
//
//    protected static String string(DataItem item) throws DocumentError {
//        if (!MajorType.UNICODE_STRING.equals(item.getMajorType())) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//        return ((UnicodeString) item).getString();
//    }
//
//    protected static Map<Integer, byte[]> mapping(Collection<RdfNQuad> deskolemizedNQuads, Map<RdfResource, RdfResource> labelMap) {
//        final RdfCanonicalizer canonicalizer = RdfCanonicalizer.newInstance(deskolemizedNQuads);
//
//        canonicalizer.canonicalize();
//
//        final Map<Integer, byte[]> verifierLabels = new HashMap<>();
//
//        for (final Map.Entry<RdfResource, RdfResource> nquad : canonicalizer.issuer().mappingTable().entrySet()) {
//            verifierLabels.put(canonLabelIndex(nquad.getValue()), Multibase.BASE_64_URL.decode(labelMap.get(nquad.getKey()).getValue().substring("_:".length())));
//        }
//
//        return verifierLabels;
//    }
//
//    protected static int canonLabelIndex(RdfResource canonBlankId) {
//        return Integer.parseInt(canonBlankId.getValue().substring("_:c14n".length()));
//    }
//
//    protected static int[] mandatory(Collection<Integer> combined, Collection<Integer> mandatory) {
//
//        final Collection<Integer> indices = new ArrayList<>();
//
//        int relative = 0;
//
//        for (int index : combined) {
//            if (mandatory.contains(index)) {
//                indices.add(relative);
//            }
//            relative++;
//        }
//        return indices.stream().mapToInt(Integer::intValue).toArray();
//    }
//
//    protected static Collection<byte[]> signatures(Collection<byte[]> signatures, Collection<Integer> mandatory, Collection<Integer> selective) {
//
//        final Collection<byte[]> filtered = new ArrayList<>();
//
//        int index = 0;
//
//        for (byte[] signature : signatures) {
//            while (mandatory.contains(index)) {
//                index++;
//            }
//            if (selective.contains(index)) {
//                filtered.add(signature);
//            }
//            index++;
//        }
//        return filtered;
//    }

    protected static int[] mandatory(int[] combined, int[] mandatory) {

        final Collection<Integer> indices = new ArrayList<>();

        int relative = 0;
        
        Arrays.sort(mandatory);

        for (int index : combined) {
            if (Arrays.binarySearch(mandatory, index) >= 0) {
                IO.println("i > " + relative + ", " + index);
                indices.add(relative);
            }
            relative++;
        }
        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void main(String[] args) {

        var c = new int[] { 0, 1, 2, 9, 12, 13, 16, 17, 18, 19 };
        var m = new int[] { 0, 12, 13, 17 };
        
        IO.println(Arrays.toString(mandatory( c, m)));

    }
}
