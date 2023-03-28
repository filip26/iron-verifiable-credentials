package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.ld.signature.proof.Proof;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Represents data integrity proof.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 *
 */
public class DataIntegrityProof implements Proof {

    /* required */
    protected URI type;

    protected URI purpose;

    /** verificationMethod */
    protected Collection<VerificationMethod> method;

    protected Instant created;

    protected byte[] value;

    /* optional */
    protected URI id;   //TODO
    
    protected String domain;

    protected String challenge;
    
    /** previousProof */
    protected URI previous; //TODO

    protected DataIntegrityProof() {
        /* protected */ }

    public DataIntegrityProof(
            URI type, 
            URI purpose, 
            Collection<VerificationMethod> method, 
            Instant created, 
            byte[] value
            ) {
        this.type = type;
        this.purpose = purpose;
        this.method = method;
        this.created = created;
        this.value = value;
        this.domain = null;
        this.challenge = null;
    }

    @Override
    public URI getType() {
        return type;
    }

    /**
     * The intent for the proof, the reason why an entity created it. Mandatory e.g.
     * assertion or authentication
     *
     * @see <a href=
     *      "https://w3c-ccg.github.io/data-integrity-spec/#proof-purposes">Proof
     *      Purposes</a>
     *
     * @return {@link URI} identifying the purpose
     */
    public URI getPurpose() {
        return purpose;
    }

    @Override
    public Collection<VerificationMethod> getMethod() {
        return method;
    }

    /**
     * The string value of an ISO8601. Mandatory
     *
     * @return the date time when the proof has been created
     */
    public Instant getCreated() {
        return created;
    }

    /**
     * A string value specifying the restricted domain of the proof.
     *
     * @return the domain or <code>null</code>
     */
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * A string value used once for a particular domain and/or time. Used to
     * mitigate replay attacks.
     * 
     * @return the challenge or <code>null</code>
     */
    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public void setValue(byte[] value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public URI previousProof() {
        return previousProof();
    }

    @Override
    public CryptoSuite getCryptoSuite() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject toJsonLd() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        // TODO Auto-generated method stub
        
    }

    @Override
    public SignatureSuite getSignatureSuite() {
        // TODO Auto-generated method stub
        return null;
    }
}
