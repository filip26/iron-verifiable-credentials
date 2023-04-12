package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

public class BaseDataIntegritySuite implements SignatureSuite {

    protected final URI id;
    protected final URI context;
    protected final CryptoSuite crypto;
    protected final LdSchema schema;

    protected BaseDataIntegritySuite(
            URI id,
            URI context,
            CryptoSuite crypto,
            LdSchema schema) {

        this.id = id;
        this.context = context;
        this.crypto = crypto;
        this.schema = schema;
    }
  
    @Override
    public URI id() {
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
