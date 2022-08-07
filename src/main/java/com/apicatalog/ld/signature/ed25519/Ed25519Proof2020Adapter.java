package com.apicatalog.ld.signature.ed25519;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.json.EmbeddedProofAdapter;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class Ed25519Proof2020Adapter extends EmbeddedProofAdapter {

    public static final String TYPE = "https://w3id.org/security#Ed25519Signature2020";

    public Ed25519Proof2020Adapter() {
        super(TYPE, new Ed25519VerificationKey2020Adapter());
    }

    @Override
    protected byte[] decodeValue(String encoding, String value) throws DocumentError {
        // verify supported proof value encoding
        if (!"https://w3id.org/security#multibase".equals(encoding)) {
            throw new DocumentError(ErrorType.Invalid, PROOF_KEY, Keywords.VALUE);
        }

        // verify proof value
        if (value == null || !Multibase.isAlgorithmSupported(value)) {
            throw new DocumentError(ErrorType.Invalid, PROOF_KEY, Keywords.VALUE);
        }

        // decode proof value
        byte[] rawProofValue = Multibase.decode(value);

        // verify proof value length
        if (rawProofValue.length != 64) {
            throw new DocumentError(ErrorType.Invalid, PROOF_KEY, Keywords.VALUE, "length");
        }

        return rawProofValue;
    }

    @Override
    protected String encodeValue(String encoding, byte[] value) throws DocumentError {
      // verify supported proof value encoding
      if (!"https://w3id.org/security#multibase".equals(encoding)) {
          throw new DocumentError(ErrorType.Invalid, PROOF_KEY, Keywords.VALUE);
      }

      if (value == null) {
          return null;
      }

      return Multibase.encode(Algorithm.Base58Btc, value);
    }

    @Override
    public Proof deserialize(JsonObject object) throws DocumentError {
        if (object == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        // data integrity checks
        if (JsonUtils.isNotObject(object)) {
            throw new DocumentError(ErrorType.Invalid, PROOF_KEY);
        }

        final JsonObject proofObject = object.asJsonObject();

        if (!JsonLdUtils.isTypeOf(TYPE, proofObject)) {

            // @type property
            if (!JsonLdUtils.hasType(proofObject)) {
                throw new DocumentError(ErrorType.Missing, PROOF_KEY, Keywords.TYPE);
            }

            throw new DocumentError(ErrorType.Unknown, "cryptoSuite", Keywords.TYPE);
        }

        return read(proofObject);
    }

    @Override
    public JsonObject serialize(Proof proof) throws DocumentError {
    return write(Json.createObjectBuilder(), proof).build();
    }
}
