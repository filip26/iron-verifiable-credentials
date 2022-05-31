package com.apicatalog.lds;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.key.VerificationKey;
import com.apicatalog.lds.proof.EmbeddedProof;
import com.apicatalog.lds.proof.ProofOptions;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class LinkedDataSignature {

    private final SignatureSuite suite;

    public LinkedDataSignature(SignatureSuite suite) {
        this.suite = suite;
    }

    /**
     * Verifies the given signed VC/VP document.
     * 
     * see
     * {@link https://w3c-ccg.github.io/data-integrity-spec/#proof-verification-algorithm}
     * 
     * @param document unsigned VC/VP document
     * @param verificationKey
     * @param signature
     * @return <code>true</code> if the document has been successfully verified
     */
    public boolean verify(final JsonObject document, final VerificationKey verificationKey, final byte[] signature) throws VerificationError {
        
        if (verificationKey == null || verificationKey.getPublicKey() == null) {
            throw new VerificationError();
        }
        
       // proof as JSON
       JsonObject proof = document.getJsonArray(EmbeddedProof.PROOF).getJsonObject(0);  //FIXME consider multiple proofs

       // FIXME use JsonLd helpers
       if (proof.containsKey(Keywords.GRAPH)) {
            proof = proof.getJsonArray(Keywords.GRAPH).getJsonObject(0);
       }

       proof = Json.createObjectBuilder(proof).remove(EmbeddedProof.PROOF_VALUE).build();
              
       // remove proof
       JsonObject data = Json.createObjectBuilder(document).remove("https://w3id.org/security#proof").build();
      
       byte[] computeSignature = hashCode(data, proof);

       return suite.verify(verificationKey.getPublicKey(), signature, computeSignature);
    }

    /**
     * Issues the given VC/VP document and returns signed version.
     * 
     * see {@link https://w3c-ccg.github.io/data-integrity-spec/#proof-algorithm}
     * 
     * @param document
     * @param options
     * @param keyPair
     * @return
     * @throws VerificationError
     */
    public JsonObject sign(JsonObject document, ProofOptions options, KeyPair keyPair) throws SigningError {

        final JsonObject proof = EmbeddedProof.from(options).toJson();

        final byte[] documentHashCode = hashCode(document, proof);

        final byte[] rawProofValue = suite.sign(keyPair.getPrivateKey(), documentHashCode);

        final String proofValue = Multibase.encode(Algorithm.Base58Btc, rawProofValue);

        return EmbeddedProof.setProof(document, proof, proofValue);
    }

    /**
     * 
     * see
     * {@link https://w3c-ccg.github.io/data-integrity-spec/#create-verify-hash-algorithm}
     * 
     * @param document
     * @param proof
     * @return
     * @throws VerificationError
     */
    public byte[] hashCode(JsonStructure document, JsonObject proof) {

        byte[] proofHash = suite.digest(suite.canonicalize(proof));

        byte[] documentHash = suite.digest(suite.canonicalize(document));

        // proof hash + document hash
        byte[] result = new byte[proofHash.length + documentHash.length];

        System.arraycopy(proofHash, 0, result, 0, proofHash.length);
        System.arraycopy(documentHash, 0, result, proofHash.length, documentHash.length);

        return result;
    }
}
