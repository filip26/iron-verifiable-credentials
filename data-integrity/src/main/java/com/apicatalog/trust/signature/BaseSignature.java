package com.apicatalog.trust.signature;

import java.util.Collection;

public interface BaseSignature extends Signature {

    Collection<String> mandatoryPointers();
    
    //TODO derive
    
}
