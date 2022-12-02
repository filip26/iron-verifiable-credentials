package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SignatureSuite;

public class DataIntegritySuite implements SignatureSuite {

    protected final LdTerm id;
    protected final URI context;
    protected final CryptoSuite crypto;
    protected final LdSchema schema;

    protected DataIntegritySuite(
            LdTerm id,
            URI context,
            CryptoSuite crypto,
            LdSchema schema) {

        this.id = id;
        this.context = context;
        this.crypto = crypto;
        this.schema = schema;
    }

    @Override
    public DataIntegrityProofOptions createOptions() {
        return new DataIntegrityProofOptions(this);
    }

    @Override
    public CryptoSuite getCryptoSuite() {
        return crypto;
    }

    @Override
    public LdSchema getSchema() {
        return schema;
    }
    
    @Override
    public LdTerm getId() {
        return id;
    }

    @Override
    public URI getContext() {
        return context;
    }
}
