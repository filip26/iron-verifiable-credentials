package com.apicatalog.di.sd;

import com.apicatalog.trust.payload.CanonicalPayload;

public interface RedactablePayload extends CanonicalPayload {

    byte[][] redactablePayload();
    
}
