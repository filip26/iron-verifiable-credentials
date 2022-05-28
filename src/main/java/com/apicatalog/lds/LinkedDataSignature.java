package com.apicatalog.lds;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.Constants;
import com.apicatalog.vc.Vc2Rdf;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.VerificationError;
import com.apicatalog.vc.VerificationError.Code;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class LinkedDataSignature {

    private final CanonicalizationAlgorithm canonicalization;
    private final DigestAlgorithm digester;
    private final SignatureAlgorithm signer;

    public LinkedDataSignature(SignatureSuite suite) {
        this(suite, suite, suite);
    }

    public LinkedDataSignature(
                CanonicalizationAlgorithm canonicalization,
                DigestAlgorithm digester,
                SignatureAlgorithm signer
            ) {
        this.canonicalization = canonicalization;
        this.digester = digester;
        this.signer = signer;
    }

    /**
     * Verifies the given signed VC/VP document.
     * 
     * see {@link https://w3c-ccg.github.io/data-integrity-spec/#proof-verification-algorithm}
     * 
     * @param document signed VC/VP document
     * @return <code>true</code> if the document has been successfully verified 
     */
    public boolean verify(JsonStructure document, byte[] publicKey) throws VerificationError {
        
//        // get verification key
//        final KeyPair verificationKey = verifiable.getProof().getVerificationMethod().get();
//        
//        if (verificationKey == null || verificationKey.getPublicKey() == null) {
//            throw new VerificationError();
//        }
//        
//        // decode verification key
//        byte[] rawVerificationKey = Multibase.decode(verificationKey.getPublicKeyMultibase());  //TODO consider other encoding
//
//        // verify verification key length - TODO needs to be clarified
//        if (rawVerificationKey.length == 32 || rawVerificationKey.length == 57 || rawVerificationKey.length == 114) {
//            throw new VerificationError(Code.InvalidProofLength);
//        }
//
//        // proof as JSON
//        JsonObject proof = verifiable.getExpandedDocument().getJsonObject(0).getJsonArray(Constants.PROOF).getJsonObject(0);  //FIXME consider multiple proofs
//        
//        // FIXME use JsonLd helpers
//        if (proof.containsKey(Keywords.GRAPH)) {
//            proof = proof.getJsonArray(Keywords.GRAPH).getJsonObject(0);
//        }
//        
//        // remove proof
//        JsonObject document = Json.createObjectBuilder(verifiable.getExpandedDocument().getJsonObject(0)).remove("https://w3id.org/security#proof").build();
//        System.out.println(document);
//        // canonicalization            
//        byte[] canonical = canonicalization.canonicalize(document);
//                    
//        byte[] documentHashCode = hashCode(canonical, proof);      //FIXME
//        
//        // decode proof value
//        byte[] rawProofValue = Multibase.decode(verifiable.getProof().getValue().getValue());
//
//        return signer.verify(rawVerificationKey, rawProofValue, documentHashCode);
        return false;
    }

    /**
     * Issues the given VC/VP document and returns signed version.
     * 
     * see {@link https://w3c-ccg.github.io/data-integrity-spec/#proof-algorithm}
     * 
     * @param document
     * @return
     * @throws VerificationError
     */
    public JsonArray sign(JsonArray document, ProofOptions options, byte[] privateKey) throws VerificationError {      //TODO use dedicated exception

        System.out.println(document);
        
        byte[] canonical = canonicalization.canonicalize(document);
        
        byte[] documentHashCode = hashCode(canonical, options);

//        
//        // decode private key
//        byte[] rawPrivateKey = Multibase.decode(privateKey.getPublicKeyMultibase());  //TODO consider other encoding
        
        byte[] rawProofValue = signer.sign(privateKey,  documentHashCode);
        
        String proofValue = Multibase.encode(rawProofValue);
        
        //TODO add proofValue, created, domain to proof
        System.out.println(proofValue);
        
        //TODO
        return null;
    }
    

    
    static byte[] reverse(byte[] data) {
        final byte[] reversed = new byte[data.length];
        for (int i=0; i<data.length; i++) {
            reversed[data.length - i - 1] = data[i];
        }
        
        
        return reversed;
    }
           
    /**
     * 
     * see {@link https://w3c-ccg.github.io/data-integrity-spec/#create-verify-hash-algorithm}
     * 
     * @param dataset
     * @return
     * @throws VerificationError
     */
    
    public byte[] hashCode(byte[] document, ProofOptions options) throws VerificationError {
        
//        proof = Json.createObjectBuilder(proof).remove(Constants.PROOF_VALUE).build();
//                
//        System.out.println(proof);

        
        byte[] proofHash = digester.digest(canonicalization.canonicalize(Vc2Rdf.toRdf(options)));
        
        byte[] documentHash = digester.digest(document);

        byte[] result = new byte[proofHash.length + documentHash.length];
        
        System.arraycopy(proofHash, 0, result, 0, proofHash.length);
        System.arraycopy(documentHash, 0, result, proofHash.length, documentHash.length);
                    
        return result;
    }
    
    public byte[] hashCode(byte[] document, JsonObject proof) throws VerificationError {
        
      proof = Json.createObjectBuilder(proof)
              .remove(Constants.PROOF_VALUE)
//              .remove(Constants.PROOF_PURPOSE)
              .remove(Keywords.TYPE)
              .build();
              
      System.out.println(proof);

      
      byte[] proofHash = digester.digest(canonicalization.canonicalize(proof));
      
      byte[] documentHash = digester.digest(document);

      byte[] result = new byte[proofHash.length + documentHash.length];
      
      System.arraycopy(proofHash, 0, result, 0, proofHash.length);
      System.arraycopy(documentHash, 0, result, proofHash.length, documentHash.length);
                  
      return result;
  }
}
