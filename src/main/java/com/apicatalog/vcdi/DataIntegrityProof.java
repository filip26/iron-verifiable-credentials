package com.apicatalog.vcdi;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.builder.GenericTreeCloner;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.traversal.NodeSelector.ProcessingPolicy;
import com.apicatalog.linkedtree.writer.NodeDebugWriter;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.method.GenericVerificationMethod;
import com.apicatalog.vc.primitive.MultibaseLiteral;
import com.apicatalog.vc.proof.DefaultProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

/**
 * Represents data integrity proof base class.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 *
 */
public class DataIntegrityProof extends DefaultProof implements Proof {

    protected final DataIntegritySuite suite;

    protected Instant created;
    protected URI purpose;

    protected String domain;
    protected String nonce;
    protected String challenge;

    protected DataIntegrityProof(
            Verifiable verifiable,
            DataIntegritySuite suite,
            CryptoSuite crypto) {
        super(verifiable, crypto);
        this.suite = suite;
    }

    public static DataIntegrityProof of(
            Verifiable verifiable,
            DataIntegritySuite suite,
            LinkedFragment source) throws AdapterError {

        NodeDebugWriter.writeToStdOut(source);

        var proofValueLiteral = source.literal(
                VcdiVocab.PROOF_VALUE.uri(),
                MultibaseLiteral.class);

        ProofValue proofValue = null;

        if (proofValueLiteral != null) {
            proofValue = suite.getProofValue(proofValueLiteral.byteArrayValue(), null);
        }

        var cryptosuite = suite.getCryptoSuite(suite.cryptosuiteName, proofValue);

        var proof = new DataIntegrityProof(verifiable, suite, cryptosuite);

        proof.created = source.xsdDateTime(VcdiVocab.CREATED.uri());

        proof.domain = source.lexeme(VcdiVocab.DOMAIN.uri());

        proof.challenge = source.lexeme(VcdiVocab.CHALLENGE.uri());

        proof.nonce = source.lexeme(VcdiVocab.NONCE.uri());

        proof.method = source.fragment(
                VcdiVocab.VERIFICATION_METHOD.uri(),
                VerificationMethod.class,
                s -> new GenericVerificationMethod(
                        s.asFragment().uri(),
                        null, // FIXME
                        null,
                        s));

        proof.purpose = source.uri(VcdiVocab.PURPOSE.uri());

        proof.previousProof = source.uri(VcdiVocab.PREVIOUS_PROOF.uri());

        proof.signature = proofValue;

        proof.ld = source;

        return proof;
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {

        super.validate(params);

        if (created == null) {
            throw new DocumentError(ErrorType.Missing, "Created");
        }
        if (purpose == null) {
            throw new DocumentError(ErrorType.Missing, "ProofPurpose");
        }
        if (params != null) {
            assertEquals(params, VcdiVocab.PURPOSE, purpose.toString()); // TODO compare as URI, expect URI in params
            assertEquals(params, VcdiVocab.CHALLENGE, challenge);
            assertEquals(params, VcdiVocab.DOMAIN, domain);
        }
    }

    @Override
    public JsonObject derive(JsonStructure context, JsonObject data, Collection<String> selectors) throws SigningError, DocumentError {
//FIXME        final ProofValue derivedProofValue = ((BaseProofValue) value).derive(context, data, selectors);

//        final JsonObject signature = LdScalar.multibase(suite.proofValueBase, derivedProofValue.toByteArray());

//        return DataIntegrityProofDraft.signed(unsignedCopy(), signature);
        throw new UnsupportedOperationException();
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
    protected LinkedTree unsignedProof(LinkedTree proof) throws DocumentError {
        try {
            var builder = new GenericTreeCloner(proof);
            return builder.deepClone((node, indexOrder, indexTerm, depth) -> VcdiVocab.PROOF_VALUE.uri().equals(indexTerm)
                    ? ProcessingPolicy.Drop
                    : ProcessingPolicy.Accept);
        } catch (TreeBuilderError e) {
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }
}
