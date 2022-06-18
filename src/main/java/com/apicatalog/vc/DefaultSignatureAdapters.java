package com.apicatalog.vc;

import java.util.Arrays;
import java.util.Collection;

import com.apicatalog.ld.signature.SignatureAdapter;
import com.apicatalog.ld.signature.SignatureAdapters;
import com.apicatalog.ld.signature.ed25519.Ed25519SignatureAdapter;

public class DefaultSignatureAdapters extends SignatureAdapters {

    protected Collection<SignatureAdapter> adapters;
    
    public DefaultSignatureAdapters() {
        super(Arrays.asList(new SignatureAdapter[] { new Ed25519SignatureAdapter() }));
    }
}
