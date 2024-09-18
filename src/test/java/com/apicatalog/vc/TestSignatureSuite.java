package com.apicatalog.vc;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.solid.SolidIssuer;
import com.apicatalog.vc.solid.SolidProofValue;
import com.apicatalog.vcdi.DataIntegritySuite;

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

    @Override
    protected CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws NodeAdapterError {
        if (TEST_CRYPTO_NAME.equals(cryptoName)) {
            return CRYPTO;
        }
        return null;
    }

    @Override
    public SolidIssuer createIssuer(KeyPair pair) {
        return new SolidIssuer(CRYPTO, pair, Multibase.BASE_58_BTC);
    }

    @Override
    protected ProofValue getProofValue(byte[] proofValue, DocumentLoader loader) throws NodeAdapterError {
        if (proofValue.length != 32) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
        }
        return new SolidProofValue(proofValue);
    }
}
