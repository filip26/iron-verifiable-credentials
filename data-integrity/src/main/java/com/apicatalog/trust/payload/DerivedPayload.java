package com.apicatalog.trust.payload;

import java.util.Collection;
import java.util.Map.Entry;


public interface DerivedPayload extends DigestiblePayload {

    Collection<byte[]> disclosedPayload();

}
