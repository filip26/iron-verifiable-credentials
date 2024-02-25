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
import com.apicatalog.vc.integrity.DataIntegrityProofDraft;
import com.apicatalog.vc.integrity.DataIntegritySuite;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.solid.SolidIssuer;
import com.apicatalog.vc.solid.SolidProofValue;

class TestSignatureSuite extends DataIntegritySuite {

    static final MethodAdapter METHOD_ADAPTER = new TestKeyAdapter();

    static final CryptoSuite CRYPTO = new CryptoSuite(
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    static final String TEST_CRYPTO_NAME = "test-2022";

    protected TestSignatureSuite() {
        super(TEST_CRYPTO_NAME, Multibase.BASE_58_BTC, METHOD_ADAPTER);
    }

    public DataIntegrityProofDraft createDraft(
            VerificationMethod method,
            URI purpose,
            Instant created,
            String domain,
            String challenge,
            String nonce) throws DocumentError {
        return new DataIntegrityProofDraft(CRYPTO,
                super.createDraft(CRYPTO, method, purpose, created, domain, challenge, nonce));
    }

    @Override
    protected CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError {
        if (TEST_CRYPTO_NAME.equals(cryptoName)) {
            return CRYPTO;
        }
        return null;
    }

    public SolidIssuer createIssuer(KeyPair pair) {
        return new SolidIssuer(this, pair, Multibase.BASE_58_BTC);
    }

    @Override
    protected ProofValue getProofValue(byte[] proofValue) throws DocumentError {
        if (proofValue.length != 32) {
            throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
        }
        return new SolidProofValue(proofValue);
    }
}
