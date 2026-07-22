package com.apicatalog.di.sd.signature;

import java.util.Collection;

import com.apicatalog.trust.signature.Signature;

public interface BaseSignature extends Signature {

    Collection<String> mandatoryPointers();

    DerivedSignature derive(Collection<String> selectors);

}
