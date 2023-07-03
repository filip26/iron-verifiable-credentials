package com.apicatalog.vc.integrity;

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

    protected static final String ID = "https://w3id.org/security#DataIntegrityProof";
    
    public static final String SEC_VOCAB = "https://w3id.org/security#";
    
    public static final LdTerm TYPE = LdTerm.create("DataIntegrityProof", SEC_VOCAB);

    protected static final LdSchema SCHEMA = DataIntegritySchema.getProof(
                TYPE,
                null,
                null
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
  
//    @Override
//    public String id() {
//        return ID;
//    }

//    @Override
//    public String context() {
//        return SEC_VOCAB;
//    }

    @Override
    public Proof readProof(JsonObject expanded) throws DocumentError {
        // TODO Auto-generated method stub
        
        LdObject ldProof = SCHEMA.read(expanded);
                
        
        
//        return new DataIntegrityProof(this, CRYPTO, ldProof, expanded);
        //FIXME
        return null;
    }

    @Override
    public VerificationMethod readMethod(JsonObject expanded) throws DocumentError {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSupported(String proofType, JsonObject expandedProof) {
        // TODO Auto-generated method stub
        return false;
    }
}
