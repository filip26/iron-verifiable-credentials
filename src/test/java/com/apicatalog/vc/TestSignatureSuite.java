package com.apicatalog.vc;

import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.ld.signature.proof.ProofType;
import com.apicatalog.vc.integrity.DataIntegritySchema;
import com.apicatalog.vc.integrity.DataIntegritySuite;

class TestSignatureSuite extends DataIntegritySuite {

    static final ProofType TYPE = new TestProofType();

    static final CryptoSuite CRYPTO = new CryptoSuite(
            TYPE.id(),
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    public TestSignatureSuite() {
        super(TYPE, CRYPTO, DataIntegritySchema.getSchema(
                LdTerm.create("TestSignatureSuite2022", "https://w3id.org/security#"),
                LdTerm.create("TestVerificationKey2022", "https://w3id.org/security#"),
                32));
    }
}
