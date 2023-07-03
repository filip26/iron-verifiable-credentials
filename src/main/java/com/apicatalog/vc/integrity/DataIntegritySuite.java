package com.apicatalog.vc.integrity;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public abstract class DataIntegritySuite implements SignatureSuite {

    protected static final String ID = VcVocab.SECURITY_VOCAB + "DataIntegrityProof";

    protected final String cryptosuite;
    
    protected DataIntegritySuite(String cryptosuite) {
        this.cryptosuite = cryptosuite;
    }

    @Override
    public boolean isSupported(String proofType, JsonObject expandedProof) {
        return ID.equals(proofType) && cryptosuite.equals(getCryptoSuite(expandedProof));
    }
    
    static String getCryptoSuite(JsonObject expandedProof) {
        if (expandedProof == null) {
            throw new IllegalArgumentException("expandedProof property must not be null.");
        }
        
        JsonValue value = expandedProof.get(DataIntegritySchema.CRYPTO_SUITE.uri());
        
        if (JsonUtils.isString(value)) {
            return ((JsonString)value).getString();
        }
        return null;        
    }
    
}
