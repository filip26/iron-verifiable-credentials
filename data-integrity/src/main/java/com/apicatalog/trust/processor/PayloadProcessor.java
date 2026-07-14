package com.apicatalog.trust.processor;

import java.util.Collection;

import com.apicatalog.trust.payload.DigestiblePayload;

public interface PayloadProcessor {

    DigestiblePayload digestible();

    void withProofs(Collection<String> ids);

}
