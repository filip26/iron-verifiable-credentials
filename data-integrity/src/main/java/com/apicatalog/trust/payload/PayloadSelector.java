package com.apicatalog.trust.payload;

import java.util.Collection;

public interface PayloadSelector extends PayloadFilter {

    DigestiblePayload digestible();

    void withProofs(Collection<String> ids);

}
