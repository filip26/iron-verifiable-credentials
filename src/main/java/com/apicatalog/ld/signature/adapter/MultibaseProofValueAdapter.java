package com.apicatalog.ld.signature.adapter;

import java.net.URI;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.multibase.Multibase;

public class MultibaseProofValueAdapter implements ProofValueAdapter {

    protected final URI id;
    
    public MultibaseProofValueAdapter(URI id) {
        this.id = id;
    }
    
    @Override
    public String encode(byte[] value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] decode(String value) throws DocumentError {
        
        // verify proof value
        if (value == null || !Multibase.isAlgorithmSupported(value)) {
            throw new DocumentError(ErrorType.Invalid, "ProofValue");
        }

        // decode proof value
        byte[] rawProofValue = Multibase.decode(value);

//        // verify proof value length
//        if (rawProofValue.length != 64) {
//            throw new DocumentError(ErrorType.Invalid, PROOF_KEY, Keywords.VALUE, "length");
//        }

        return rawProofValue;
    }

    @Override
    public URI id() {
        return id;
    }
}
