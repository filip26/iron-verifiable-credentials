package com.apicatalog.di.sd;

import java.util.Collection;
import java.util.Map.Entry;

import com.apicatalog.trust.payload.RedactablePayload;

class BasePayload implements RedactablePayload {

    byte[] base;
    Collection<Entry<Integer, byte[]>> redactable;
    
    @Override
    public byte[] canonicalPayload() {
        return base;
    }

    @Override
    public void digest(String algorithm, byte[] value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public byte[] digest(String algorithm) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> digestAlgorithms() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Entry<Integer, byte[]>> redactablePayload() {
        return redactable;
    }

}
