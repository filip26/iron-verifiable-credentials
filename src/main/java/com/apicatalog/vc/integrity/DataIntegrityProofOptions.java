package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.jsonld.schema.LdObject;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.vc.method.VerificationMethod;
import com.apicatalog.vc.suite.SignatureSuite;

@Deprecated
public class DataIntegrityProofOptions  {

    protected final SignatureSuite suite;

    protected URI type;

    protected URI purpose;

    protected VerificationMethod method;

    protected Instant created;

    /* optional */
    protected String domain;

    protected String challenge;
    
    public DataIntegrityProofOptions(final SignatureSuite suite) {
        this.suite = suite;
        this.type = URI.create(suite.id().uri());
    }

    public SignatureSuite getSuite() {
        return suite;
    }

    public LdObject toUnsignedProof() {

        Map<String, Object> proof = new LinkedHashMap<>();

        proof.put(LdTerm.TYPE.uri(), type);
        proof.put(DataIntegrity.CREATED.uri(), created);
        proof.put(DataIntegrity.PURPOSE.uri(), purpose);
        proof.put(DataIntegrity.VERIFICATION_METHOD.uri(), method);
        proof.put(DataIntegrity.DOMAIN.uri(), domain);
        proof.put(DataIntegrity.CHALLENGE.uri(), challenge);

        return new LdObject(proof);
    }

    public DataIntegrityProofOptions verificationMethod(VerificationMethod verificationMethod) {
        this.method = verificationMethod;
        return this;
    }

    public DataIntegrityProofOptions purpose(URI purpose) {
        this.purpose = purpose;
        return this;
    }

    public DataIntegrityProofOptions created(Instant created) {
        this.created = created;
        return this;
    }

    public DataIntegrityProofOptions domain(String domain) {
        this.domain = domain;
        return this;
    }

    public DataIntegrityProofOptions challenge(String challenge) {
        this.challenge = challenge;
        return this;
    }

}
