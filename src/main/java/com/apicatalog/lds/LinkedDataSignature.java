package com.apicatalog.lds;

import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.key.VerificationKey;
import com.apicatalog.lds.proof.EmbeddedProof;

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
     * @param document expanded unsigned VC/VP document
     * @param proof expanded proof with no proofValue
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
     * @param document expanded unsigned VC/VP document
     * @param proof expanded proof options
     * @param keyPair
     * @return computed signature
     * @throws VerificationError
     */
    //FIXME change order, kayPar, options - align with Vc api
    public byte[] sign(JsonObject document, JsonObject options, KeyPair keyPair) throws SigningError {

        final byte[] documentHashCode = hashCode(document, options);

        return suite.sign(keyPair.getPrivateKey(), documentHashCode);
    }

    /**
     * see
     * {@link https://w3c-ccg.github.io/data-integrity-spec/#create-verify-hash-algorithm}
     *
     * @param document expanded unsigned VC/VP document
     * @param proof expanded proof with no proofValue
     * @return computed hash code
     * 
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
