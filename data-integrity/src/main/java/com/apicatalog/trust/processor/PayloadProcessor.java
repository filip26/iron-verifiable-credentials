package com.apicatalog.trust.processor;

import java.util.Collection;

import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.signature.Signature;

public interface PayloadProcessor {

    //FIXME replace signature with some Options, enables issuing
    default RedactablePayload redactable(Signature signature, Collection<String> mandatoryPointers) {
        throw new UnsupportedOperationException("Selective discosure is not supported by this processor.");
    }

    DigestiblePayload digestible();

    void withProofs(Collection<String> ids);

}
