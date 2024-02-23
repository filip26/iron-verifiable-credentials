package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.integrity.DataIntegritySuite;
import com.apicatalog.vc.proof.ProofValue;

class TestSignatureSuite extends DataIntegritySuite {

    static final CryptoSuite CRYPTO = new CryptoSuite(
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    static final String TEST_CRYPTO_NAME = "test-2022";

    protected TestSignatureSuite() {
        super(TEST_CRYPTO_NAME, new TestKeyAdapter(), Multibase.BASE_58_BTC);
    }

    protected void validateProofValue(byte[] proofValue) throws DocumentError {
        if (proofValue != null && proofValue.length != 32) {
            throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
        }
    }

    public DataIntegrityProof createDraft(
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
    protected ProofValue getProofValue(byte[] encoded) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError {
        if (TEST_CRYPTO_NAME.equals(cryptoName)) {
            return CRYPTO;
        }
        return null;
    }
}
