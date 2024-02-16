package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.model.ProofValueProcessor;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * Represents data integrity proof base class.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 *
 */
public class DataIntegrityProof implements Proof, ProofValueProcessor, MethodAdapter {

    protected final DataIntegritySuite suite;
    protected final CryptoSuite crypto;

    protected URI id;
    protected URI purpose;
    protected VerificationMethod method;
    protected Instant created;
    protected String domain;
    protected String nonce;
    protected String challenge;
    protected byte[] value;
    protected URI previousProof;

    final JsonObject expanded;

    protected static final Collection<String> V1_CONTEXTS = Arrays.asList(
            "https://w3id.org/security/data-integrity/v2",
            "https://w3id.org/security/multikey/v1"
            );

    protected static final Collection<String> V2_CONTEXTS = Arrays.asList(
            "https://www.w3.org/ns/credentials/v2"
            );
    
    protected DataIntegrityProof(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            JsonObject expandedProof) {
        this.suite = suite;
        this.crypto = crypto;
        this.expanded = expandedProof;
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
    public VerificationMethod getMethod() {
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

    /**
     * A string value used once for a particular domain and/or time. Used to
     * mitigate replay attacks.
     * 
     * @return the challenge or <code>null</code>
     */
    public String getChallenge() {
        return challenge;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public URI previousProof() {
        return previousProof;
    }

    @Override
    public CryptoSuite getCryptoSuite() {
        return crypto;
    }

    @Override
    public JsonObject toJsonLd() {
        return expanded;
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        if (created == null) {
            throw new DocumentError(ErrorType.Missing, "Created");
        }
        if (method == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethod");
        }
        if (purpose == null) {
            throw new DocumentError(ErrorType.Missing, "ProofPurpose");
        }
        if (value == null || value.length == 0) {
            throw new DocumentError(ErrorType.Missing, "ProofValue");
        }

        assertEquals(params, DataIntegrityVocab.PURPOSE, purpose.toString()); // TODO compare as URI, expect URI in params
        assertEquals(params, DataIntegrityVocab.CHALLENGE, challenge);
        assertEquals(params, DataIntegrityVocab.DOMAIN, domain);

        suite.validateProofValue(value);
    }

    @Override
    public JsonObject removeProofValue(JsonObject expanded) {
        return Json.createObjectBuilder(expanded).remove(DataIntegrityVocab.PROOF_VALUE.uri()).build();
    }

    @Override
    public JsonObject setProofValue(JsonObject expanded, byte[] proofValue) throws DocumentError {

        LdNodeBuilder node = new LdNodeBuilder(Json.createObjectBuilder(expanded));

        node.set(DataIntegrityVocab.PROOF_VALUE)
                .scalar("https://w3id.org/security#multibase",
                        Multibase.BASE_58_BTC.encode(proofValue));

        return node.build();
    }

    @Override
    public MethodAdapter methodProcessor() {
        return this;
    }

    @Override
    public VerificationMethod read(JsonObject expanded) throws DocumentError {
        return suite.methodAdapter.read(expanded);
    }

    @Override
    public Collection<String> getContext(ModelVersion model) {
        if (ModelVersion.V11.equals(model)) {
            return V1_CONTEXTS;
        }
        return V2_CONTEXTS;
    }

    @Override
    public ProofValueProcessor valueProcessor() {
        return this;
    }

    @Override
    public JsonObject write(VerificationMethod value) {
        throw new UnsupportedOperationException();
    }

    public String getNonce() {
        return nonce;
    }

    protected static void assertEquals(Map<String, Object> params, Term name, String param) throws DocumentError {

        final Object value = params.get(name.name());

        if (value == null) {
            return;
        }

        if (!value.equals(param)) {
            throw new DocumentError(ErrorType.Invalid, name);
        }
    }
}
