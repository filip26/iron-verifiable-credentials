package com.apicatalog.vcdi;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.FragmentComposer;
import com.apicatalog.linkedtree.json.JsonFragment;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.literal.ByteArrayValue;
import com.apicatalog.linkedtree.orm.Adapter;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Mapper;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.linkedtree.xsd.XsdDateTimeAdapter;
import com.apicatalog.multibase.MultibaseLiteral;
import com.apicatalog.vc.model.ModelValidation;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.proof.BaseProofValue;
import com.apicatalog.vc.proof.DerivedProofValue;
import com.apicatalog.vc.proof.LinkedProof;
import com.apicatalog.vc.proof.ProofValue;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents data integrity proof base class.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 *
 */
@Fragment
@Context("https://w3id.org/security/data-integrity/v2")
@Vocab("https://w3id.org/security#")
public interface DataIntegrityProof extends LinkedProof {

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
    @Term("proofPurpose")
    @Override
    URI purpose();

    /**
     * The string value of an ISO8601. Mandatory
     *
     * @return the date time when the proof has been created
     */
    @Vocab("http://purl.org/dc/terms/")
    @Adapter(XsdDateTimeAdapter.class)
    Instant created();

    @Adapter(XsdDateTimeAdapter.class)
    @Term(value = "expiration", compact = false)
    Instant expires();

    /**
     * A string value specifying the restricted domain of the proof.
     *
     * @return the domain or <code>null</code>
     */
    @Term
    String domain();

    /**
     * A string value used once for a particular domain and/or time. Used to
     * mitigate replay attacks.
     * 
     * @return the challenge or <code>null</code>
     */
    @Term
    String challenge();

    @Term
    String nonce();

    @Term("proofValue")
    @Provided
    @Override
    ProofValue signature();

    @Term("cryptosuite")
    @Provided
    @Override
    @Mapper(CryptoSuiteMapper.class)
    CryptoSuite cryptosuite();

    /**
     * Get data integrity suite bound to this proof.
     * 
     * @return a data integrity suite
     */
    @Provided
    DataIntegritySuite di();
    
    @Override
    default void validate(Map<String, Object> params) throws DocumentError {

        ModelValidation.assertNotNull(this::purpose, VcdiVocab.PURPOSE);
        ModelValidation.assertNotNull(this::signature, VcdiVocab.PROOF_VALUE);
        ModelValidation.assertNotNull(this::cryptosuite, VcdiVocab.CRYPTO_SUITE);

        if (cryptosuite().isUnknown()) {
            throw new DocumentError(ErrorType.Unknown, VcdiVocab.CRYPTO_SUITE);
        }

        if (method() != null && method().id() == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethodId");
        }

        if (created() != null && expires() != null && created().isAfter(expires())) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }

        if (params != null) {
            ModelValidation.assertEquals(params, VcdiVocab.PURPOSE, purpose());
            ModelValidation.assertEquals(params, VcdiVocab.CHALLENGE, challenge());
            ModelValidation.assertEquals(params, VcdiVocab.DOMAIN, domain());
            ModelValidation.assertEquals(params, VcdiVocab.NONCE, nonce());
        }
    }

    @Override
    default void verify(VerificationKey key) throws VerificationError, DocumentError {
        if (created() != null && Instant.now().isBefore(created())) {
            throw new DocumentError(ErrorType.Invalid, "Created");
        }
        if (expires() != null && Instant.now().isAfter(expires())) {
            throw new VerificationError(VerificationErrorCode.Expired);
        }
        LinkedProof.super.verify(key);
    }

    @Override
    default JsonObject derive(Collection<String> selectors) throws CryptoSuiteError, DocumentError {

        ModelValidation.assertNotNull(this::signature, VcdiVocab.PROOF_VALUE);

        if (signature() instanceof BaseProofValue baseProofValue) {

            System.out.println("DERIVE " + baseProofValue);
            System.out.println("     > " + selectors);
            DerivedProofValue proofValue = baseProofValue.derive(selectors);
            System.out.println("> " + proofValue);
            System.out.println("> " + proofValue.getClass());
            
            if (proofValue instanceof ByteArrayValue byteArrayValue) {
                System.out.println("> " + di().proofValueBase.encode(byteArrayValue.byteArrayValue()));
            }

//            final Collection<VerifiableMaterial> proofs = new LinkedList<>(model.proofs());
//            proofs.add(signedProof);

            
//            proofValue.documentModel().of(null, null);
            System.out.println("D: " + document());
            
            try {
                DataIntegrityProof proof = FragmentComposer.create()
                        .set("id", id())
                        .set("cryptosuite", cryptosuite())
                        .set("purpose", purpose())
                        .set("created", created())
                        .set("expires", expires())
                        .set("method", method())
                        .set("di", di())
                        .set(VcdiVocab.PREVIOUS_PROOF.name(), previousProof())
                        .set(VcdiVocab.CHALLENGE.name(), challenge())
                        .set(VcdiVocab.NONCE.name(), nonce())
                        .set(VcdiVocab.DOMAIN.name(), domain())
                        .json(di().writer::compact)
                        .get(DataIntegrityProof.class);
               return null; 
//                return ((JsonFragment)proof);
            } catch (NodeAdapterError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            //TODO clone DI proof and set value, return the proof
//            System.out.println("> " + baseProofValue.document());
//            System.out.println("> " + baseProofValue.proof());
        }
        
        // JsonFragment resi to ze pak Proof, Verifiable, bude dedit it toto!!!
//        if (this instanceof JsonFragment) {
//            
//        }
        
        throw new UnsupportedOperationException("The proof does not support a selective disclosure.");
//            byte[] signature = proofValue.;
//            
//            DataIntegrityProof.sign(
//                    baseProofValue.proof(),
//                    Json.createValue(suite.proofValueBase.encode(signature)))

//            sign(baseProofValue.proof(), )

//            final JsonObject signature = LdScalar.multibase(suite.proofValueBase, derivedProofValue.toByteArray());
//
//            return DataIntegrityProofDraft.signed(unsignedCopy(), signature);
//            
//            final VerifiableMaterial signedProof = draft.sign(unsignedDraft, ldSignature.value());
//
//            if (signedProof == null) {
//                throw new IllegalStateException();
//            }
//
//            final VerifiableMaterial signedDocument = model
//                    .withProof(signedProof)
//                    .materialize();
//
//            return JsonLdContext.set(signedDocument.context(), signedDocument.compacted());

    }

    static VerifiableMaterial sign(VerifiableMaterial proof, JsonValue signature) throws DocumentError {
        JsonObject compacted = Json.createObjectBuilder(proof.compacted())
                .add(VcdiVocab.PROOF_VALUE.name(), signature)
                .build();

        JsonObject expanded = Json.createObjectBuilder(proof.expanded())
                .add(VcdiVocab.PROOF_VALUE.uri(),
                        Json.createArrayBuilder().add(
                                Json.createObjectBuilder()
                                        .add(JsonLdKeyword.VALUE, signature)
                                        .add(JsonLdKeyword.TYPE, MultibaseLiteral.typeName())))
                .build();

        return new GenericMaterial(
                proof.context(),
                compacted,
                expanded);
    }
}
