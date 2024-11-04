package com.apicatalog.vc.di;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Literal;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.linkedtree.xsd.XsdDateTimeAdapter;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;

/**
 * Represents data integrity proof base class.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 *
 */
@Fragment
@Context("https://w3id.org/security/data-integrity/v2")
@Vocab("https://w3id.org/security#")
public interface DataIntegrityProof extends Proof {

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
    @Literal(XsdDateTimeAdapter.class)
    Instant created();

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

    @Literal(XsdDateTimeAdapter.class)
    @Term(value = "expiration", compact = false)
    Instant expires();
    
    @Term("proofValue")
    @Provided
    @Override
    ProofValue signature();
    
    @Term("cryptosuite")
    @Provided
    @Override
    CryptoSuite cryptoSuite();
    
    @Override
    default void validate(Map<String, Object> params) throws DocumentError {

        if (purpose() == null) {
            throw new DocumentError(ErrorType.Missing, "ProofPurpose");
        }
        if (cryptoSuite() == null) {
            throw new DocumentError(ErrorType.Missing, "CryptoSuite");
        }
                
        if (params != null) {
//            assertEquals(params, VcdiVocab.PURPOSE, purpose().toString()); // TODO compare as URI, expect URI in params
//            assertEquals(params, VcdiVocab.CHALLENGE, challenge());
//            assertEquals(params, VcdiVocab.DOMAIN, domain());
        }
    }
}
