package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.integrity.DataIntegritySuite;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.proof.SolidSignature;

class TestSignatureSuite extends DataIntegritySuite<SolidSignature> {

    static final CryptoSuite CRYPTO = new CryptoSuite(
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    static final String TEST_CRYPTO_NAME = "test-2022";

    protected TestSignatureSuite() {
        super(TEST_CRYPTO_NAME, new TestKeyAdapter());
    }

    public DataIntegrityProof<SolidSignature> createDraft(
            VerificationMethod method,
            URI purpose,
            Instant created,
            String domain,
            String challenge,
            String nonce
            ) throws DocumentError {
        return super.createDraft(CRYPTO, method, purpose, created, domain, challenge, nonce);
    }

    @Override
    protected CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError {
        if (TEST_CRYPTO_NAME.equals(cryptoName)) {
            return CRYPTO;
        }
        return null;
    }

    @Override
    protected SolidSignature createProofValue() {
        return new SolidSignature(Multibase.BASE_58_BTC) {
            @Override
            public void validate() throws DocumentError {
                if (value != null && value.length != 32) {
                    throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
                }
            }
        };
    }
    
    public TestIssuer createIssuer(KeyPair pair) {
        return new TestIssuer(this, pair);
    }
}
