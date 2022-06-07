package com.apicatalog.lds;

import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
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
    public boolean verify(final JsonObject document, final JsonObject proof, final VerificationKey verificationKey, final byte[] signature) throws VerificationError {

        if (verificationKey == null || verificationKey.getPublicKey() == null) {
            throw new VerificationError();
        }

       final JsonObject proofObject = Json.createObjectBuilder(proof).remove(EmbeddedProof.PROOF_VALUE).build();

       final byte[] computeSignature = hashCode(document, proofObject);

       return suite.verify(verificationKey.getPublicKey(), signature, computeSignature);
    }

    /**
     * Issues the given VC/VP document and returns the document signature.
     *
     * see {@link https://w3c-ccg.github.io/data-integrity-spec/#proof-algorithm}
     *
     * @param document
     * @param options
     * @param keyPair
     * @return
     * @throws VerificationError
     */
    //FIXME must return the signature as byte[]
    //FIXME change order, kayPar, options - align with Vc api
    public JsonObject sign(JsonObject document, ProofOptions options, KeyPair keyPair) throws SigningError {

        final JsonObject data = EmbeddedProof.removeProof(document);
        final JsonObject proof = EmbeddedProof.from(options).toJson();

        final byte[] documentHashCode = hashCode(data, proof);

        final byte[] rawProofValue = suite.sign(keyPair.getPrivateKey(), documentHashCode);

        //FIXME encoding depends on proof @type - move to Vc api
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

    public KeyPair keygen(int length) {

        com.apicatalog.lds.algorithm.SignatureAlgorithm.KeyPair keyPair = suite.keygen(length);

        Ed25519KeyPair2020 kp = new Ed25519KeyPair2020(null); //FIXME
        kp.setPublicKey(keyPair.getPublicKey());
        kp.setPrivateKey(keyPair.getPrivateKey());
        return kp;
    }
}
