package com.apicatalog.vc.integrity;

import java.net.URI;
import java.util.Map;

import com.apicatalog.jsonld.schema.LdObject;
import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

public final class DataIntegritySuite implements SignatureSuite {

    protected static final URI ID = URI.create("");
    protected static final URI CONTEXT = URI.create("");
    
    public static final String SEC_VOCAB = "https://w3id.org/security#";

    
    public static final LdTerm TYPE = LdTerm.create("DataIntegrityProof", 
            
            SEC_VOCAB);

    
    protected static final LdSchema SCHEMA = DataIntegritySchema.getProof(
                TYPE
//                Algorithm.Base58Btc,
//                key -> key.length == 32
            );
    
    protected final Map<String, CryptoSuite> crypto;

    protected DataIntegritySuite(Map<String, CryptoSuite> crypto) {
        this.crypto = crypto;
    }
    
    public static final DataIntegritySuite getInstance(CryptoSuite suite) {
        //TODO
        return null;
    }
  
    @Override
    public URI id() {
        return ID;
    }

    @Override
    public URI context() {
        return CONTEXT;
    }

    @Override
    public Proof readProof(JsonObject expanded) throws DocumentError {
        // TODO Auto-generated method stub
        
        LdObject o = SCHEMA.read(expanded);
        
        System.out.println(o.entrySet());
        
        return null;
    }

    @Override
    public VerificationMethod readMethod(JsonObject expanded) throws DocumentError {
        // TODO Auto-generated method stub
        return null;
    }
}
