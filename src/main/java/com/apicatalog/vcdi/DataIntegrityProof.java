package com.apicatalog.vcdi;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.orm.Adapter;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Mapper;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.linkedtree.xsd.XsdDateTimeAdapter;
import com.apicatalog.vc.model.ModelValidation;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.proof.VerifiableProof;

/**
 * Represents data integrity proof base class.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 *
 */
@Fragment
@Context("https://w3id.org/security/data-integrity/v2")
@Vocab("https://w3id.org/security#")
public interface DataIntegrityProof extends VerifiableProof {

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
        VerifiableProof.super.verify(key);
    }
}
