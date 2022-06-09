package com.apicatalog.lds.ed25519;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.lds.DataError;
import com.apicatalog.lds.DataError.ErrorType;
import com.apicatalog.lds.proof.EmbeddedProof;
import com.apicatalog.lds.proof.Proof;
import com.apicatalog.multibase.Multibase;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class Ed25519Proof2020 extends EmbeddedProof implements Proof {

    public static final String TYPE = "https://w3id.org/security#Ed25519Signature2020";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void updateValue(String encoding, String value) throws DataError {
            
        // verify supported proof value encoding
        if (!"https://w3id.org/security#multibase".equals(encoding)) {
            throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE);
            //FIXME belongs to ED25...
        }

        // verify proof value
        if (value == null || !Multibase.isAlgorithmSupported(value)) {
            throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE);
        }

        // decode proof value
        byte[] rawProofValue = Multibase.decode(value);

        // verify proof value length
        if (rawProofValue.length != 64) {
            throw new DataError(ErrorType.Invalid, "proof", Keywords.VALUE, "length");
        }

        this.value = rawProofValue;
        
    }

}
