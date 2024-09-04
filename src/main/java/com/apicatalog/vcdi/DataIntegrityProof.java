package com.apicatalog.vcdi;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.linkedtree.Link;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.builder.GenericTreeBuilder;
import com.apicatalog.linkedtree.primitive.LinkableObject;
import com.apicatalog.linkedtree.traversal.NodeSelector.ProcessingPolicy;
import com.apicatalog.vc.lt.MultibaseLiteral;
import com.apicatalog.vc.lt.ObjectFragmentMapper;
import com.apicatalog.vc.method.GenericVerificationMethod;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.proof.DefaultProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vcdm.EmbeddedProof;

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
            DataIntegritySuite suite,
            CryptoSuite crypto) {
        super(crypto);
        this.suite = suite;
    }

    public static LinkedFragment of(
            Link id,
            Collection<String> types,
            Map<String, LinkedContainer> properties,
            Supplier<LinkedTree> rootSupplier,
            DataIntegritySuite suite) throws DocumentError {

        var selector = new ObjectFragmentMapper(properties);

        var proofValueLiteral = selector.single(VcdiVocab.PROOF_VALUE, MultibaseLiteral.class);

        ProofValue proofValue = null;

        if (proofValueLiteral != null) {
            // TODO document loader???
            proofValue = suite.getProofValue(proofValueLiteral.byteArrayValue(), null);
        }

        var cryptosuite = suite.getCryptoSuite(suite.cryptosuiteName, proofValue);

        var proof = new DataIntegrityProof(suite, cryptosuite);

        proof.created = selector.xsdDateTime(VcdiVocab.CREATED);

        proof.domain = selector.lexeme(VcdiVocab.DOMAIN);

        proof.challenge = selector.lexeme(VcdiVocab.CHALLENGE);

        proof.nonce = selector.lexeme(VcdiVocab.NONCE);

        proof.method = selector.single(
                VcdiVocab.VERIFICATION_METHOD,
                method -> {
                    if (method instanceof VerificationMethod verificationMethod) {
                        return verificationMethod;
                    }
                    return new GenericVerificationMethod(
                            selector.id(VcdiVocab.VERIFICATION_METHOD),
                            null,
                            null,
                            method.ld());
                });

        proof.purpose = selector.id(VcdiVocab.PURPOSE);

        proof.signature = proofValue;

        proof.previousProof = selector.id(VcdiVocab.PREVIOUS_PROOF);

        proof.fragment = new LinkableObject(id, types, properties, rootSupplier, proof);

        return proof.fragment;
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
    public MethodAdapter methodProcessor() {
        return suite.methodAdapter;
    }

    @Override
    protected LinkedTree unsigned(LinkedTree verifiable) {
        return EmbeddedProof.removeProofs(verifiable);
    }

    @Override
    protected LinkedTree unsignedProof(LinkedTree proof) {
        var builder = new GenericTreeBuilder(proof);
        return builder.deepClone((node, indexOrder, indexTerm, depth) -> VcdiVocab.PROOF_VALUE.uri().equals(indexTerm)
                ? ProcessingPolicy.Drop
                : ProcessingPolicy.Accept);
    }
}
