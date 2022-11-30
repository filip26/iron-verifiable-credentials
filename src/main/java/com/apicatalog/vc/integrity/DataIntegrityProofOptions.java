package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.ld.signature.proof.ProofOptions;

public class DataIntegrityProofOptions extends DataIntegrityProof implements ProofOptions {

    protected final SignatureSuite suite;

    public DataIntegrityProofOptions(final SignatureSuite suite, URI purpose, VerificationMethod method, Instant created) {
        this.suite = suite;
        this.type = suite.getProofType().id();
        this.purpose = purpose;
        this.method = method;
        this.created = created;
    }

    @Override
    public SignatureSuite getSuite() {
        return suite;
    }

    @Override
    public LdObject toUnsignedProof() {
        return null;
    }
}
