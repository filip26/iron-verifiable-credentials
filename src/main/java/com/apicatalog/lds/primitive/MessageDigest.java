package com.apicatalog.lds.primitive;

import java.security.NoSuchAlgorithmException;

import com.apicatalog.lds.algorithm.DigestAlgorithm;

public class MessageDigest implements DigestAlgorithm {

    private final String type;
    
    public MessageDigest(String type) {
        this.type = type;
    }
    

    @Override
    public byte[] digest(byte[] data) {
        
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(type);
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }
}
