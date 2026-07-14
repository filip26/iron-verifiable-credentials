package com.apicatalog.trust.processor;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.payload.DerivedPayload;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.RedactablePayload;

public interface PayloadProcessor {

    // FIXME replace signature with some Options, enables issuing
    default RedactablePayload redactable(Collection<String> mandatoryPointers, Map<String, Object> options) {
        throw new UnsupportedOperationException("Selective discosure is not supported by this processor.");
    }

    default DerivedPayload derived(Map<String, Object> options) {
        throw new UnsupportedOperationException("Selective discosure is not supported by this processor.");
    }

    DigestiblePayload digestible();

    void withProofs(Collection<String> ids);

}
