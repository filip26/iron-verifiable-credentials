package com.apicatalog.lds;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.Constants;
import com.apicatalog.vc.VerificationError;

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
    public JsonObject sign(JsonObject document, ProofOptions options, byte[] privateKey) throws VerificationError { // TODO
                                                                                                                    // use
        JsonObject proof = createProof(options);

        byte[] canonical = suite.canonicalize(document);

        byte[] documentHashCode = hashCode(canonical, proof);

        byte[] rawProofValue = suite.sign(privateKey, documentHashCode);

        String proofValue = Multibase.encode(rawProofValue);

        return Json.createObjectBuilder(document)
                .add(Constants.PROOF,
                        Json.createArrayBuilder().add(
                        Json.createObjectBuilder(proof).add(Constants.PROOF_VALUE,
                                Json.createArrayBuilder().add(Json.createObjectBuilder()
                                        .add(Keywords.VALUE, proofValue)
                                        .add(Keywords.TYPE, "https://w3id.org/security#multibase"))))
                ).build();
    }

    /**
     * 
     * see
     * {@link https://w3c-ccg.github.io/data-integrity-spec/#create-verify-hash-algorithm}
     * 
     * @param dataset
     * @return
     * @throws VerificationError
     */
    public byte[] hashCode(byte[] document, JsonObject proof) throws VerificationError {

        proof = Json.createObjectBuilder(proof).remove(Constants.PROOF_VALUE)
//              .remove(Constants.PROOF_PURPOSE)
                .remove(Keywords.TYPE).build();

        byte[] proofHash = suite.digest(suite.canonicalize(proof));

        byte[] documentHash = suite.digest(document);

        byte[] result = new byte[proofHash.length + documentHash.length];

        System.arraycopy(proofHash, 0, result, 0, proofHash.length);
        System.arraycopy(documentHash, 0, result, proofHash.length, documentHash.length);

        return result;
    }

    static JsonObject createProof(ProofOptions options) {
        return Json.createObjectBuilder().add(Keywords.TYPE, Json.createArrayBuilder().add(options.getType()))

                .add(Constants.PROOF_VERIFICATION_METHOD,
                        Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add(Keywords.ID,
                                        options.getVerificationMethod().getId().toString())))
                .add(Constants.CREATED,
                        Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add(Keywords.TYPE, "http://www.w3.org/2001/XMLSchema#dateTime")
                                        .add(Keywords.VALUE, options.getCreated().toString())

                                ))
                .add(Constants.PROOF_PURPOSE,
                        Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add(Keywords.ID,
                                        "https://w3id.org/security#assertionMethod")))
                // TODO domain to proof

                .build();
    }
}
