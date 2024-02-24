package com.apicatalog.vc.proof;

import java.util.Objects;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.multibase.Multibase;

import jakarta.json.JsonObject;

/**
 * Represent a proof value used together with full disclosure suites. i.e.
 * suites do not allowing a selective disclosure.
 */
public abstract class SolidSignature implements ProofValue {

    protected byte[] value;
    protected Multibase base;

    protected SolidSignature(Multibase base) {
        this.base = base;
    }

    @Override
    public void verify(CryptoSuite cryptoSuite, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError {

        Objects.requireNonNull(value);
        Objects.requireNonNull(data);
        Objects.requireNonNull(publicKey);

        if (cryptoSuite == null) {
            throw new VerificationError(Code.UnsupportedCryptoSuite);
        }

        final LinkedDataSignature signature = new LinkedDataSignature(cryptoSuite);

        // verify signature
        signature.verify(
                data,
                unsignedProof,
                publicKey,
                value);
    }

    @Override
    public void set(LdScalar scalar) throws DocumentError {
        value = scalar.multibase(base);
    }

    @Override
    public void set(byte[] signature) {
        value = signature;
    }

    @Override
    public JsonObject expand() {
        return LdScalar.encode("https://w3id.org/security#multibase", base.encode(value));
    }

    @Override
    public int length() {
        return value != null ? value.length : 0;
    }

    /*
     *         return new LdNodeBuilder(Json.createObjectBuilder(expanded))
                .set(DataIntegrityVocab.PROOF_VALUE)
                .value(value.encoded())
                .build();

     */
//    public String encoded

//    @Override
//    public void validate() throws DocumentError {
//        if (value != null && value.length != length) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
//        }
//    }
//    

//    

}
