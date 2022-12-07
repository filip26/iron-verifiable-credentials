package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.vc.integrity.DataIntegrity;
import com.apicatalog.vc.integrity.DataIntegritySuite;

class TestSignatureSuite extends DataIntegritySuite {

    static final LdTerm ID = LdTerm.create("TestSignatureSuite2022", VcVocab.SECURITY_VOCAB);

    static final URI CONTEXT = URI.create("classpath:data-integrity-test-signature-2022.jsonld");

    static final CryptoSuite CRYPTO = new CryptoSuite(
            ID,
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    public TestSignatureSuite() {
        super(ID, CONTEXT, CRYPTO, DataIntegrity.getProof(
                LdTerm.create("TestSignatureSuite2022", "https://w3id.org/security#"),
                Algorithm.Base58Btc,
                key -> key.length == 32,
                DataIntegrity.getVerificationKey(
                        LdTerm.create("TestVerificationKey2022", "https://w3id.org/security#"),
                        DataIntegrity.getPublicKey(
                                Algorithm.Base58Btc,
                                Codec.Ed25519PublicKey,
                                (key) -> key == null || key.length > 0
                        )
                )));
    }
}
