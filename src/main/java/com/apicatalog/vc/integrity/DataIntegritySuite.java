package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.vc.method.VerificationMethod;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

public class DataIntegritySuite implements SignatureSuite {

    protected final LdTerm id;
    protected final URI context;
    protected final CryptoSuite crypto;
    protected final LdSchema schema;

    protected DataIntegritySuite(
            LdTerm id,
            URI context,
            CryptoSuite crypto,
            LdSchema schema) {

        this.id = id;
        this.context = context;
        this.crypto = crypto;
        this.schema = schema;
    }

//    @Override
//    public DataIntegrityProofOptions createOptions() {
//        return new DataIntegrityProofOptions(this);
//    }
  
    @Override
    public LdTerm id() {
        return id;
    }

    @Override
    public URI context() {
        return context;
    }

    @Override
    public Proof readProof(JsonObject expanded) {
        // TODO Auto-generated method stub
        
        return null;
    }

    @Override
    public VerificationMethod readMethod(JsonObject expanded) throws DocumentError {
        // TODO Auto-generated method stub
        return null;
    }
}
