package com.apicatalog.ed25519;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;
import com.apicatalog.rdf.io.error.RdfWriterException;
import com.apicatalog.rdf.io.error.UnsupportedContentException;
import com.apicatalog.vc.Constants;
import com.apicatalog.vc.VcDocument;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.VerificationError;
import com.apicatalog.vc.VerificationError.Code;
import com.apicatalog.vc.proof.VerificationKey;

import io.setl.rdf.normalization.RdfNormalize;
import jakarta.json.Json;
import jakarta.json.JsonObject;

public class Ed25519Signature2020 {

    static {
    }
  
    
    /**
     * Verifies the given signed VC/VP document.
     * 
     * see {@link https://w3c-ccg.github.io/data-integrity-spec/#proof-verification-algorithm}
     * 
     * @param verifiable signed VC/VP document
     * @return <code>true</code> if the document has been successfully verified 
     */
    public boolean verify(Verifiable verifiable) throws VerificationError {
        
        // get verification key
        final VerificationKey verificationKey = verifiable.getProof().getVerificationMethod().get();
        
        if (verificationKey == null || verificationKey.getPublicKeyMultibase() == null) {
            throw new VerificationError();
        }
        
        // decode verification key
        byte[] rawVerificationKey = Multibase.decode(verificationKey.getPublicKeyMultibase());

        // verify verification key length - TODO needs to be clarified
        if (rawVerificationKey.length == 32 || rawVerificationKey.length == 57 || rawVerificationKey.length == 114) {
            throw new VerificationError(Code.InvalidProofLength);
        }

        // proof as JSON
        JsonObject proof = verifiable.getExpandedDocument().getJsonObject(0).getJsonArray(Constants.PROOF).getJsonObject(0);  //FIXME consider multiple proofs
        
        // FIXME use JsonLd helpers
        if (proof.containsKey(Keywords.GRAPH)) {
            proof = proof.getJsonArray(Keywords.GRAPH).getJsonObject(0);
        }
        
        // remove proof
        JsonObject document = Json.createObjectBuilder(verifiable.getExpandedDocument().getJsonObject(0)).remove("https://w3id.org/security#proof").build();
        System.out.println(document);
        // canonicalization            
        byte[] canonical = canonicalize(document);
                    
        byte[] documentHashCode = hashCode(canonical, proof);
        
        // decode proof value
        byte[] rawProofValue = Multibase.decode(verifiable.getProof().getValue().getValue());

        return verify(rawVerificationKey, rawProofValue, documentHashCode);            
    }
    
    static byte[] reverse(byte[] data) {
        final byte[] reversed = new byte[data.length];
        for (int i=0; i<data.length; i++) {
            reversed[data.length - i - 1] = data[i];
        }
        
        
        return reversed;
    }
    
    public Verifiable issue(VcDocument document) throws VerificationError {      //TODO use dedicated exception

        
        //TODO
        return null;
    }
    

       
    /**
     * 
     * see {@link https://w3c-ccg.github.io/data-integrity-spec/#create-verify-hash-algorithm}
     * 
     * @param dataset
     * @return
     * @throws VerificationError
     */
    
    static byte[] hashCode(byte[] document, JsonObject proof) throws VerificationError {
        
        proof = Json.createObjectBuilder(proof).remove(Constants.PROOF_VALUE).build();
        
        //FIXME remove
        proof = Json.createObjectBuilder(proof).add(Constants.PROOF_VERIFICATION_METHOD, "https://example.com/issuer/123#key-0").build();
        
        System.out.println(proof);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            byte[] proofHash = digest.digest(canonicalize(proof));
            
            byte[] documentHash = digest.digest(document);

            byte[] result = new byte[proofHash.length + documentHash.length];
            
            System.arraycopy(proofHash, 0, result, 0, proofHash.length);
            System.arraycopy(documentHash, 0, result, proofHash.length, documentHash.length);
                        
            return result;

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            throw new VerificationError(e);
        }
    }
    
    static boolean verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        
        try {            
            Signature suite = Signature.getInstance("Ed25519");

            suite.initVerify(getPublicKey(publicKey));
            suite.update(data);

            boolean r = suite.verify(signature);
            System.out.println("V: " + r);
            return r;
 
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        return false;

    }

    static PublicKey getPublicKey(byte[] publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {

//      val lastIndex = publicKey.lastIndex
//      val lastByte = publicKey[lastIndex]
//      val lastByteAsInt = lastByte.toInt()
//      val isXOdd = lastByteAsInt.and(255).shr(7) == 1
//
//      publicKey[lastIndex] = (lastByteAsInt and 127).toByte()
//
//      var y = publicKey.reversedArray().asBigInteger;

//      var keyFactory = KeyFactory.getInstance("Ed25519");
//      var nameSpec = NamedParameterSpec.ED25519;
//      var point = new EdECPoint(isXOdd, publicKey);
//      var keySpec = new EdECPublicKeySpec(nameSpec, point);
//      var key = keyFactory.generatePublic(keySpec);

        
        byte[] pk = Arrays.copyOfRange(publicKey, 2, publicKey.length -2);

        //TODO validate the key starts with 0xed01
        //System.out.println(Integer.toHexString((publicKey[0] << 8)  + publicKey[1] ) );
        
        // key is already converted from hex string to a byte array.
        KeyFactory kf = KeyFactory.getInstance("Ed25519");
        
        // determine if x was odd.
        boolean xisodd = false;
        int lastbyteInt = pk[pk.length - 1];
        if ((lastbyteInt & 255) >> 7 == 1) {
            xisodd = true;
        }
        // make sure most significant bit will be 0 - after reversing.
        pk[pk.length - 1] &= 127;
              
       pk = reverse(pk);
        BigInteger y = new BigInteger(1, pk);

        NamedParameterSpec paramSpec = new NamedParameterSpec("Ed25519");
        EdECPoint ep = new EdECPoint(xisodd, y);
        EdECPublicKeySpec pubSpec = new EdECPublicKeySpec(paramSpec, ep);
        PublicKey pub = kf.generatePublic(pubSpec);
        return pub;
    }
    
    static final byte[] canonicalize(JsonObject document) throws VerificationError {
        
        // canonicalization
        try {
            RdfDataset dataset = JsonLd.toRdf(JsonDocument.of(document)).get();
            
            RdfDataset canonical = RdfNormalize.normalize(dataset);
            
            StringWriter writer = new StringWriter();
            
            RdfWriter rdfWriter = Rdf.createWriter(MediaType.N_QUADS, writer);
            
            rdfWriter.write(canonical);

System.out.println(">>> " + writer.toString().substring(0, writer.toString().length() -1) + "'");
            return writer.toString()
                    .substring(0, writer.toString().length() -1)
                    .getBytes(StandardCharsets.UTF_8);

            
        } catch (JsonLdError e) {
            throw new VerificationError(e);     //FIXME ...
            
        } catch (UnsupportedContentException e) {
            throw new VerificationError(e);
            
        } catch (IOException e) {
            throw new VerificationError(e);
            
        } catch (RdfWriterException e) {
            throw new VerificationError(e);
            
        }
    }
}
