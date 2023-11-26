package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.integrity.DataIntegritySuite;

class TestSignatureSuite extends DataIntegritySuite {

    static final String CONTEXT = "classpath:data-integrity-test-signature-2022.jsonld";

    static final CryptoSuite CRYPTO = new CryptoSuite(
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    static final String TEST_CRYPTO_NAME = "test-2022";


    protected TestSignatureSuite() {
        super(TEST_CRYPTO_NAME, new TestKeyAdapter());
    }

    @Override
    protected CryptoSuite getCryptoSuite(String cryptoName) throws DocumentError {
        if (TEST_CRYPTO_NAME.equals(cryptoName)) {
            return CRYPTO;
        }
        return null;
    }

    public DataIntegrityProof createDraft(
            VerificationMethod method,
            URI purpose,
            Instant created,
            String domain,
            String challenge) throws DocumentError {
        return super.createDraft(CRYPTO, method, purpose, created, domain, challenge);
    }

//    @Override
//    public Proof readProof(JsonObject proof) throws DocumentError {
//        return TestSignatureProof.readProof(proof);
//    }
//
//    @Override
//    public boolean isSupported(String proofType, JsonObject proof) {
//        return ID.equals(proofType);
//    }
}
