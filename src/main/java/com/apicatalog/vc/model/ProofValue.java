package com.apicatalog.vc.model;

/**
 * Raw proof value (i.e. a signature) and curve name.
 * 
 * @since 0.9.1
 */
public class ProofValue {

    protected String curve;
    protected byte[] value;
    
    public String curve() {
        return curve;
    }
    
    public byte[] raw() {
        return value;
    }
}
