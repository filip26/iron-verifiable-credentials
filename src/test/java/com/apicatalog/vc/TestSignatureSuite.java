package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdAdapter;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.vc.integrity.DataIntegrityMethodReader;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.integrity.DataIntegritySuite;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

class TestSignatureSuite extends DataIntegritySuite {

    static final String ID = VcVocab.SECURITY_VOCAB + "TestSignatureSuite2022";

    static final String CONTEXT = "classpath:data-integrity-test-signature-2022.jsonld";

    static final CryptoSuite CRYPTO = new CryptoSuite(
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    static final String TEST_CRYPTO_NAME = "test-2022";

    static LdAdapter<byte[]> proofValueAdapter = new LdAdapter<byte[]>() {

        @Override
        public JsonObject write(byte[] value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] read(JsonObject value) throws DocumentError {
            // TODO Auto-generated method stub
            return null;
        }
    };

    protected TestSignatureSuite() {
        super(TEST_CRYPTO_NAME, new DataIntegrityMethodReader(), proofValueAdapter);
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
