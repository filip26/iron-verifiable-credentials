package com.apicatalog.vc;

import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.vc.issuer.StandardIssuer;
import com.apicatalog.vc.proof.SolidSignature;

public class TestIssuer extends StandardIssuer<SolidSignature> {
    public TestIssuer(TestSignatureSuite suite, KeyPair keyPair) {
        super(keyPair, suite);
    }
}
