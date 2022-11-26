package com.apicatalog.vc.integrity;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.PropertyName;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.adapter.MethodAdapter;
import com.apicatalog.ld.signature.adapter.ProofAdapter;
import com.apicatalog.ld.signature.algorithm.CanonicalizationAlgorithm;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.proof.ProofOptions;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class DataIntegritySuite<P extends DataIntegrityProof> extends CryptoSuite implements SignatureSuite<P> {
    
    protected final ProofAdapter proofAdapter;
    protected final Collection<MethodAdapter> methodAdapter;

    protected DataIntegritySuite(
            URI id, 
            CanonicalizationAlgorithm canonicalization, 
            DigestAlgorithm digester, 
            SignatureAlgorithm signer) {
        super(id, canonicalization, digester, signer);
        this.proofAdapter = null;
        this.methodAdapter = null;

    }

    @Override
    public ProofAdapter<P> getProofAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MethodAdapter getMethodAdapter(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    //FIXME should  return something what enforces all required fields
    
    @Override
    public DataIntegrityProofBuilder<DataIntegrityProofOptions> createOptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertyName proofValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertyName proofMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonValue encodeProofValue(byte[] value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] decodeProofValue(JsonValue value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] decodeVerificationKey(JsonObject objectO) {
        // TODO Auto-generated method stub
        return null;
    }
    

}
