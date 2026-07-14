package com.apicatalog.di.sd;

import java.util.Collection;

import com.apicatalog.trust.payload.CanonicalPayload;

public interface SDPayload extends CanonicalPayload {

    Collection<byte[]> redactablePayload();
    
}
