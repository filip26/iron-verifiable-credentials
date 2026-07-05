package com.apicatalog.trust.data;

import java.util.Collection;
import java.util.Set;

public interface Data {

    String c14n();

    default DigestiblePayload digestiblePayload() {
        return digestiblePayload(Set.of());
    }

    default void digestiblePayload(DigestiblePayload payload) {
        digestiblePayload(Set.of(), payload);
    }

    // enables proof chains
    DigestiblePayload digestiblePayload(Collection<String> withProofs);

    void digestiblePayload(Collection<String> withProofs, DigestiblePayload payload);

}
