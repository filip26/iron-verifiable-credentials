package com.apicatalog.trust.payload;

import java.util.Collection;

@FunctionalInterface
public interface PayloadFilter {

    RedactablePayload filter(Collection<String> mandatoryPointers);

}
