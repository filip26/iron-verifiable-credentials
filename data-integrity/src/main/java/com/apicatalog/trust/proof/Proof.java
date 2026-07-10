package com.apicatalog.trust.proof;

import java.time.Instant;
import java.util.Collection;

import com.apicatalog.trust.payload.CanonicalPayload;
import com.apicatalog.trust.signature.Signature;

public interface Proof extends CanonicalPayload {

    String type();

    /**
     * Retrieves the cryptographic signature associated with this proof. If a
     * signature is present, the proof is considered signed and its authenticity can
     * be verified against the canonical representation.
     *
     * @return the {@link Signature} object, or {@code null} if the proof is
     *         unsigned
     */
    Signature signature();

    String verificationMethod();

    String purpose();

    Instant created();

    @Deprecated
    Collection<String> previous();
}
