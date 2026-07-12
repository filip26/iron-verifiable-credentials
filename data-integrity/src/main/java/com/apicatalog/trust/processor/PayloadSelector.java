package com.apicatalog.trust.processor;

import java.util.Collection;

import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.signature.Signature;

public interface PayloadSelector {

    default RedactablePayload redactable(Signature signature, Collection<String> mandatoryPointers) {
        throw new UnsupportedOperationException("Selective discosure is not supported by this selector.");
    }

    DigestiblePayload digestible();

    void withProofs(Collection<String> ids);

}
