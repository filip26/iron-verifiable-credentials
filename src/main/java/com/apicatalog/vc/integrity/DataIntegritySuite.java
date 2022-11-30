package com.apicatalog.vc.integrity;

import java.util.Map;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.adapter.MethodAdapter;
import com.apicatalog.ld.signature.adapter.ProofValueAdapter;
import com.apicatalog.ld.signature.proof.ProofType;

public class DataIntegritySuite implements SignatureSuite {
    
    protected final ProofType type;
    protected final CryptoSuite crypto;
    
    protected final ProofValueAdapter proofValueAdapter;
    protected final Map<String, MethodAdapter> methodAdapter;

    protected DataIntegritySuite(
            ProofType type, 
            CryptoSuite crypto,
            ProofValueAdapter proofAdapter,
            Map<String, MethodAdapter> methodAdapter
            ) {

        this.type = type;
        this.crypto = crypto;

        this.proofValueAdapter = proofAdapter;
        this.methodAdapter = methodAdapter;

    }

//    @Override
//    public MethodAdapter getMethodAdapter(String type) {
//        return methodAdapter.get(type);
//    }

    //FIXME should  return something what enforces all required fields
    
    @Override
    public DataIntegrityProofBuilder createOptions() {
        // TODO Auto-generated method stub
        return null;
    }
//
//    @Override
//    public ProofValueAdapter getProofValueAdapter() {
//        return proofValueAdapter;
//    }
//    
    @Override
    public ProofType getProofType() {
        return type;
    }
    
    @Override
    public CryptoSuite getCryptoSuite() {
        return crypto;
    }

    @Override
    public LdSchema getSchema() {
        // TODO Auto-generated method stub
        return null;
    }
}
