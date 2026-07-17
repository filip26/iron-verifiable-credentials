package com.apicatalog.di.sd;

import com.apicatalog.trust.payload.CanonicalPayload;

public interface SDPayload extends CanonicalPayload {

    byte[][] redactablePayload();
    
}
