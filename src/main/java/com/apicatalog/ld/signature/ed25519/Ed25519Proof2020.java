package com.apicatalog.ld.signature.ed25519;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.proof.EmbeddedProof;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class Ed25519Proof2020 extends EmbeddedProof implements Proof {

    protected static final String TYPE = "Ed25519Signature2020";

    public static boolean isIstanceOf(final JsonValue object) {
        return JsonLdUtils.isTypeOf(BASE + TYPE,  object);
    }

    public static Ed25519Proof2020 from(ProofOptions options) {

        if ((BASE + TYPE).equals(options.getType())) {
            final Ed25519Proof2020 proof = new Ed25519Proof2020();

            proof.verificationMethod = options.getVerificationMethod();
            proof.created = options.getCreated();
            proof.domain = options.getDomain();
            proof.purpose = options.getPurpose();
            proof.domain = options.getDomain();

            return proof;
        }

        //TODO
        throw new IllegalStateException();
    }

    public static EmbeddedProof from(final JsonValue json, final DocumentLoader loader) throws DataError {

        if (json == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        // data integrity checks
        if (JsonUtils.isNotObject(json)) {
            throw new DataError(ErrorType.Invalid, PROOF);
        }

        final JsonObject proofObject = json.asJsonObject();

        if (!JsonLdUtils.isTypeOf(BASE + Ed25519Proof2020.TYPE, proofObject)) {

            // @type property
            if (!JsonLdUtils.hasType(proofObject)) {
                throw new DataError(ErrorType.Missing, PROOF, Keywords.TYPE);
            }

            throw new DataError(ErrorType.Unknown, "cryptoSuite", Keywords.TYPE);
        }

        EmbeddedProof embeddedProof = new Ed25519Proof2020();

        return EmbeddedProof.from(embeddedProof, proofObject, loader);
    }


    @Override
    public String getType() {
        return BASE + TYPE;
    }

    @Override
    public void setValue(String encoding, String value) throws DataError {

        // verify supported proof value encoding
        if (!"https://w3id.org/security#multibase".equals(encoding)) {
            throw new DataError(ErrorType.Invalid, PROOF, Keywords.VALUE);
        }

        // verify proof value
        if (value == null || !Multibase.isAlgorithmSupported(value)) {
            throw new DataError(ErrorType.Invalid, PROOF, Keywords.VALUE);
        }

        // decode proof value
        byte[] rawProofValue = Multibase.decode(value);

        // verify proof value length
        if (rawProofValue.length != 64) {
            throw new DataError(ErrorType.Invalid, PROOF, Keywords.VALUE, "length");
        }

        this.value = rawProofValue;
    }

    @Override
    public String getValue(String encoding) throws DataError {

        // verify supported proof value encoding
        if (!"https://w3id.org/security#multibase".equals(encoding)) {
            throw new DataError(ErrorType.Invalid, PROOF, Keywords.VALUE);
        }

        if (value == null) {
            return null;
        }

        return Multibase.encode(Algorithm.Base58Btc, value);
    }

}
