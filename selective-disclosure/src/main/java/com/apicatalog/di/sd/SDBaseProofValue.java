package com.apicatalog.di.sd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;
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

public class SDBaseProofValue implements Signature {

    static final byte[] BYTE_PREFIX = new byte[] { (byte) 0xd9, 0x5d, 0x00 };

    private String signatureAlgorithm;
    private String digestAlgorithm;

    private Proof proof;
    private RedactablePayload payload;
    
    private byte[] baseSignature;
    private byte[] proofPublicKey;
    private byte[] hmacKey;

    private Collection<byte[]> signatures;
    private Collection<String> mandatoryPointers;

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
            PayloadProcessor data) {

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

            final var proofValue = new SDBaseProofValue();
            proofValue.signatureAlgorithm = signatureAlgorithm;
            proofValue.digestAlgorithm = digestAlgorithm;

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
//            IO.println("> signatures: " + proofValue.signatures.size());
            proofValue.mandatoryPointers = new ArrayList<>(((Array) top.getDataItems().get(4)).getDataItems().size());

            for (final DataItem item : ((Array) top.getDataItems().get(4)).getDataItems()) {
                proofValue.mandatoryPointers.add(string(item));
            }
//            IO.println("> mandatory pointers: " + proofValue.mandatoryPointers);

            proofValue.payload = data.redactable(
                    proofValue.mandatoryPointers,
                    Map.of("HMAC_KEY", proofValue.hmacKey)
                    );

            return proofValue;

        } catch (CborException e) {
            throw new IllegalArgumentException(e);
//          throw new DocumentError(e, ErrorType.Invalid, "ProofValue");
        }
    }
    
    public static SDBaseProofValue generateSignature(
            String algorithm, 
            AsymmetricSigner baseSigner, 
            byte[] proofPublicKey,
            AsymmetricSigner proofSigner,
            MessageDigest digestor, 
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
       proofValue.signatureAlgorithm = algorithm;
       proofValue.digestAlgorithm = "SHA-256";  //FIXME
       
       proofValue.baseSignature = baseSigner.sign(digest);
       proofValue.hmacKey = ((PayloadWithHMAC)payload).hmacKey();
       proofValue.proofPublicKey = proofPublicKey;
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
            Function<String, MessageDigest> digestFactory,
            byte[] publicKey)
            throws InvalidKeyException, SignatureException {

        if (signatures.size() != payload.redactablePayload().size()) {
            return false;
        }
        
        var digestor = digestFactory.apply(digestAlgorithm);

        var proofDigest = digestor.digest(proof.canonicalPayload());
        var dataDigest = digestor.digest(payload.canonicalPayload());
//IO.println(new String(payload.canonicalPayload()));
        var digest = hash(
                proofDigest,
                proofPublicKey,
                dataDigest);
//IO.println("> digest:" + digest.length);
////        var digest = digest(digestor, proof.canonicalPayload(), data.digestiblePayload(proof.previous()));
////System.out.println(new String(data.digestiblePayload(Set.of()).canonicalPayload()));
//IO.println(HexFormat.of().formatHex(proofDigest));
//IO.println(HexFormat.of().formatHex(dataDigest));

        var isBaseSignatureVerified = verifier.verify(publicKey, digest, baseSignature);
        
        if (!isBaseSignatureVerified) {
            return false;
        }
        
        var proofKeyCodec = MulticodecDecoder.newInstance().getCodec(proofPublicKey)
                .orElseThrow();
        
        //TODO must match signature algo -> limitation because of verifier ...
        
        var decodedProofPublicKey = proofKeyCodec.decode(proofPublicKey);
        
        var redactableIterator = payload.redactablePayload().iterator();
        
        for (var signature :signatures) {
            
            var redactable = redactableIterator.next();
            
            if (!verifier.verify(decodedProofPublicKey, redactable.getValue(), signature)) {
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

    public byte[] hmacKey() {
        return hmacKey;
    }
}
