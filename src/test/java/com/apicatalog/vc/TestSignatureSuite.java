package com.apicatalog.vc;

import java.net.URI;
import java.util.List;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.primitive.MessageDigest;
import com.apicatalog.cryptosuite.primitive.Urdna2015;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.solid.SolidIssuer;
import com.apicatalog.vc.solid.SolidProofValue;
import com.apicatalog.vcdi.DataIntegrityProofDraft;
import com.apicatalog.vcdi.DataIntegritySuite;

class TestSignatureSuite extends DataIntegritySuite {

    static final String TEST_CRYPTO_NAME = "test-2022";

    static final CryptoSuite CRYPTO = new CryptoSuite(
            TEST_CRYPTO_NAME,
            256,
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    protected TestSignatureSuite() {
        super(TEST_CRYPTO_NAME, TestDataIntegrityProof.class, List.of(TestMultikey.class), Multibase.BASE_58_BTC);
    }

    @Override
    protected CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) {
        if (TEST_CRYPTO_NAME.equals(cryptoName)) {
            return CRYPTO;
        }
        return null;
    }

    @Override
    public SolidIssuer createIssuer(KeyPair pair) {
        return new SolidIssuer(
                this,
                CRYPTO,
                pair,
                Multibase.BASE_58_BTC,
                method -> new DataIntegrityProofDraft(this, CRYPTO, method));
    }

    @Override
    protected ProofValue getProofValue(Proof proof, VerifiableMaterial data, VerifiableMaterial proofMaterial, byte[] proofValue, DocumentLoader loader, URI base) throws DocumentError {
        if (proofValue == null) {
            return null;
        }

        if (proofValue.length != 32) {
            throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
        }
        return SolidProofValue.of(CRYPTO, data, proofMaterial, proofValue, proof);
    }
}
