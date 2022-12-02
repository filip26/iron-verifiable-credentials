package com.apicatalog.vc.integrity;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.proof.ProofType;

public class DataIntegritySuite implements SignatureSuite {

    protected final ProofType type;
    protected final CryptoSuite crypto;
    protected final LdSchema schema;

    protected DataIntegritySuite(
            ProofType type,
            CryptoSuite crypto,
            LdSchema schema) {

        this.type = type;
        this.crypto = crypto;
        this.schema = schema;
    }

    @Override
    public DataIntegrityProofOptions createOptions() {
        return new DataIntegrityProofOptions(this);
    }

    @Override
    public ProofType getProofType() {
        return type;
    }

    @Override
    public CryptoSuite getCryptoSuite() {
        return crypto;
    }

    @Override
    public LdSchema getSchema() {
        return schema;
    }
}
