package com.apicatalog.trust.payload;

import java.util.Collection;
import java.util.Map.Entry;


public interface RedactablePayload extends DigestiblePayload {

    Collection<Entry<Integer, byte[]>> redactablePayload();

    Collection<String> pointers();
    
}
