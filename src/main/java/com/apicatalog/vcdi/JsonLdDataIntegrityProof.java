package com.apicatalog.vcdi;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.linkedtree.adapter.LinkedFragmentAdapter;
import com.apicatalog.linkedtree.selector.StringValueSelector;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.proof.BaseProofValue;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

/**
 * Represents data integrity proof base class.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 *
 */
public class JsonLdDataIntegrityProof implements Proof, MethodAdapter {

    protected final DataIntegritySuite suite;
    protected final CryptoSuite crypto;
    protected final JsonObject expanded;

    protected URI id;
    protected URI purpose;
    protected VerificationMethod method;
    protected Instant created;
    protected String domain;
    protected String nonce;
    protected String challenge;
    protected ProofValue value;
    protected URI previousProof;

    protected JsonLdDataIntegrityProof(
            DataIntegritySuite suite,
            CryptoSuite crypto,
            JsonObject expandedProof) {
        this.suite = suite;
        this.crypto = crypto;
        this.expanded = expandedProof;
    }

    @Override
    public void verify(Collection<String> context, JsonObject data, VerificationKey method) throws VerificationError, DocumentError {

        Objects.requireNonNull(value);
        Objects.requireNonNull(data);
        Objects.requireNonNull(method);

        // remove a proof value and get a new unsigned copy
        final JsonObject unsignedProof = unsignedCopy();

        // verify signature
        value.verify(
                crypto,
                context,
                data,
                unsignedProof,
                method.publicKey());
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
        if (value == null) {
            throw new DocumentError(ErrorType.Missing, "ProofValue");
        }

        if (params != null) {
            assertEquals(params, DataIntegrityVocab.PURPOSE, purpose.toString()); // TODO compare as URI, expect URI in params
            assertEquals(params, DataIntegrityVocab.CHALLENGE, challenge);
            assertEquals(params, DataIntegrityVocab.DOMAIN, domain);
        }
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
    public VerificationMethod method() {
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

    public String nonce() {
        return nonce;
    }

    @Override
    public ProofValue signature() {
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
    public CryptoSuite cryptoSuite() {
        return crypto;
    }

    @Override
    public MethodAdapter methodProcessor() {
        return this;
    }

//    @Override
//    public VerificationMethod read(LinkedData expanded) throws DocumentError {
//        return suite.methodAdapter.read(expanded);
//    }

//    @Override
//    public LinkedNode write(VerificationMethod value) {
//        throw new UnsupportedOperationException();
//    }

    protected static void assertEquals(Map<String, Object> params, Term name, String param) throws DocumentError {
        final Object value = params.get(name.name());

        if (value == null) {
            return;
        }

        if (!value.equals(param)) {
            throw new DocumentError(ErrorType.Invalid, name);
        }
    }
    
    protected JsonObject unsignedCopy() {
        return Json.createObjectBuilder(expanded).remove(DataIntegrityVocab.PROOF_VALUE.uri()).build();
    }

    @Override
    public JsonObject derive(JsonStructure context, JsonObject data, Collection<String> selectors) throws SigningError, DocumentError {

        final ProofValue derivedProofValue = ((BaseProofValue)value).derive(context, data,  selectors);
        
        final JsonObject signature = LdScalar.multibase(suite.proofValueBase, derivedProofValue.toByteArray());

        return DataIntegrityProofDraft.signed(unsignedCopy(), signature);
    }

    @Override
    public LinkedFragmentAdapter resolve(String id, Collection<String> types, StringValueSelector stringSelector) {
        // TODO Auto-generated method stub
        return null;
    }  
}
